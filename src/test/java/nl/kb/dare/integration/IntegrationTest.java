package nl.kb.dare.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.junit.DropwizardAppRule;
import nl.kb.dare.App;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.oai.OaiRecordResult;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;

public class IntegrationTest {
    private static final String APP_HOST = "localhost:4567";
    private static final String APP_URL = "http://" + APP_HOST;
    private static final String OAI_URL = "http://localhost:18081/oai";

    private static final IntegrationSocketClientStatus socketStatus = new IntegrationSocketClientStatus();

    @ClassRule
    public static TestRule oaiRule = new DropwizardAppRule<>(OaiTestServer.class,
            IntegrationTest.class.getResource("/integration/oai-test-server.yaml").getPath());

    @ClassRule
    public static TestRule instanceRule = new DropwizardAppRule<>(App.class,
            IntegrationTest.class.getResource("/integration/integration.yaml").getPath());

    @BeforeClass
    public static void setup() throws ClassNotFoundException, SQLException, IOException {
        Class.forName("org.h2.Driver");
        final Connection con = DriverManager.getConnection(
                "jdbc:h2:mem:dareintegration", "daredev", "daredev");

        final Statement statement = con.createStatement();

        final String schemaSql = IOUtils.toString(IntegrationTest.class.getResourceAsStream("/integration/db/schema.sql"), "UTF8");

        for (String statementSql : schemaSql.split(";")) {
            statement.addBatch(statementSql);
        }

        statement.executeBatch();
        statement.close();
    }

    @Test
    public void run() throws Exception {

        // Add a websocket client to keep track of progress
        final WebSocketClient webSocketClient = new WebSocketClient();
        final StatusClientSocket socket = new StatusClientSocket();

        webSocketClient.start();
        webSocketClient.connect(socket, new URI(String.format("ws://%s/status-socket", APP_HOST)), new ClientUpgradeRequest());

        // First create a new repository configuration via HTTP POST to app url
        final String locationOfNewlyCreatedRepository = createRepository(new Repository(
                OAI_URL,
                "Integration test OAI",
                "nl_didl_norm",
                "test",
                null,
                false));

        // Next enable it by executing a PUT to the returned location
        if (!enableRepository(locationOfNewlyCreatedRepository)) {
            fail("failed to enable repository: " + locationOfNewlyCreatedRepository);
        }

        // Start a waiter thread for the first harvest to finish
        final Thread waitForFirstHarvest = getFirstHarvestWaiter();
        waitForFirstHarvest.start();

        // Then start the harvester (which is disabled by default)
        if (!startHarvester()) { fail("failed to start the harvester"); }

        // Wait for the first harvest to finish
        waitForFirstHarvest.join();

        // Make some assertions now
        final List<OaiRecord> records = getRecords().getResult();
        assertThat(records, containsInAnyOrder(
                hasProperty("processStatus", is(ProcessStatus.PENDING)),
                hasProperty("processStatus", is(ProcessStatus.PENDING)),
                hasProperty("processStatus", is(ProcessStatus.PENDING)),
                hasProperty("processStatus", is(ProcessStatus.PENDING))
        ));


    }

    private boolean startHarvester() throws IOException {
        final HttpClient httpClient = HttpClientBuilder.create().build();
        final HttpPut httpPut = new HttpPut(String.format("%s/harvesters/start", APP_URL));
        httpPut.addHeader("Accept", "application/json");

        final HttpResponse response = httpClient.execute(httpPut);
        return response.getStatusLine().getStatusCode() == 200;
    }

    private boolean enableRepository(String locationOfNewlyCreatedRepository) throws IOException {
        final HttpClient httpClient = HttpClientBuilder.create().build();

        final HttpPut httpPut = new HttpPut(String.format("%s/%s", locationOfNewlyCreatedRepository, "enable"));
        final HttpResponse response = httpClient.execute(httpPut);
        return response.getStatusLine().getStatusCode() == 200;
    }

    private String createRepository(Repository repository) throws IOException {
        final HttpClient httpClient = HttpClientBuilder.create().build();
        final HttpPost httpPost = new HttpPost(String.format("%s/repositories", APP_URL));
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.addHeader("Accept", "application/json");
        httpPost.setEntity(new StringEntity(new ObjectMapper().writeValueAsString(repository)));
        final HttpResponse httpResponse = httpClient.execute(httpPost);

        return httpResponse.getFirstHeader("Location").getValue();
    }

    private OaiRecordResult getRecords() throws IOException {
        final HttpClient httpClient = HttpClientBuilder.create().build();
        final HttpGet httpGet = new HttpGet(String.format("%s/records", APP_URL));
        httpGet.addHeader("Accept", "application/json");
        final HttpResponse response = httpClient.execute(httpGet);

        final String data = IOUtils.toString(response.getEntity().getContent(), "UTF8");

        return new ObjectMapper().readValue(data, OaiRecordResult.class);
    }

    private Thread getFirstHarvestWaiter() {
        return new Thread(() -> {

            boolean isRunning = false;
            boolean hasRun = false;
            while (!hasRun) {
                switch (socketStatus.getStatus().harvesterStatus.harvesterRunState) {
                    case RUNNING:
                        isRunning = true;
                        break;
                    case WAITING:
                        hasRun = isRunning;
                        break;
                    default:
                }
                try {
                    Thread.sleep(5L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @WebSocket
    public class StatusClientSocket {
        private final CountDownLatch closeLatch;

        StatusClientSocket() {
            this.closeLatch = new CountDownLatch(1);
        }

        public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
            return this.closeLatch.await(duration,unit);
        }

        @OnWebSocketClose
        public void onClose(int statusCode, String reason) {
            System.out.println("Wesbocket connection closed");
        }

        @OnWebSocketConnect
        public void onConnect(Session session) {
            System.out.println("Websocket connected");
        }

        @OnWebSocketMessage
        public void onMessage(String msg) {
            synchronized (socketStatus) {
                try {
                    // System.out.println(msg);
                    socketStatus.setStatus(new ObjectMapper().readValue(msg, SocketStatusUpdate.class));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}

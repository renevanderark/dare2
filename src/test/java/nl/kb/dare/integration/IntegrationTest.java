package nl.kb.dare.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import io.dropwizard.testing.junit.DropwizardAppRule;
import nl.kb.dare.App;
import nl.kb.dare.integration.crud.CrudOperations;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;

public class IntegrationTest {
    private static final String APP_HOST = "localhost:4567";
    public static final String APP_URL = "http://" + APP_HOST;
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

    @AfterClass
    public static void tearDown() throws IOException {
        FileUtils.deleteDirectory(new File("./1"));
    }

    @Before
    public void startWebsocket() throws Exception {
        // Add a websocket client to keep track of progress
        final WebSocketClient webSocketClient = new WebSocketClient();
        final StatusClientSocket socket = new StatusClientSocket();
        webSocketClient.start();
        webSocketClient.connect(socket, new URI(String.format("ws://%s/status-socket", APP_HOST)), new ClientUpgradeRequest());

    }

    @Test
    public void run() throws Exception {

        // First create a new repository configuration via HTTP POST to app url
        final String locationOfNewlyCreatedRepository = CrudOperations.createRepository(new Repository(
                OAI_URL,
                "Integration test OAI",
                "nl_didl_norm",
                "test",
                null,
                false));

        // Next enable it by executing a PUT to the returned location
        if (!CrudOperations.enableRepository(locationOfNewlyCreatedRepository)) {
            fail("failed to enable repository: " + locationOfNewlyCreatedRepository);
        }

        // Make sure initial harvest for this repository is succesful
        testInitialHarvest();

        // Make sure records are processed properly
        testFirstRecordProcessingRun();

    }


    private void testInitialHarvest() throws IOException, InterruptedException {
        // Start a waiter thread for the first harvest to finish
        final Thread waitForFirstHarvest = getFirstHarvestWaiter();
        waitForFirstHarvest.start();

        // Then start the harvester (which is disabled by default)
        if (!CrudOperations.startHarvester()) { fail("failed to start the harvester"); }

        // Wait for the first harvest to finish
        waitForFirstHarvest.join();

        // Based on the mock responses there should be 4 records pending
        assertThat(CrudOperations.getRecords().getResult(), containsInAnyOrder(
                hasProperty("processStatus", is(ProcessStatus.PENDING)),
                hasProperty("processStatus", is(ProcessStatus.PENDING)),
                hasProperty("processStatus", is(ProcessStatus.PENDING)),
                hasProperty("processStatus", is(ProcessStatus.PENDING))
        ));
    }

    private void testFirstRecordProcessingRun() throws IOException, InterruptedException {
        // Start a waiter thread for all records to be processed
        final Thread waitForRecordsProcessed = getRecordProcessingWaiter();
        waitForRecordsProcessed.start();

        // Then start the record processor (which is disabled by default)
        if (!CrudOperations.startRecordProcessor()) { fail("failed to start the record processor"); }

        // Wait for all records to be processed
        waitForRecordsProcessed.join();

        // Based on the mock responses all 4 records should be succesfully processed
        assertThat(CrudOperations.getRecords().getResult(), containsInAnyOrder(
                hasProperty("processStatus", is(ProcessStatus.PROCESSED)),
                hasProperty("processStatus", is(ProcessStatus.PROCESSED)),
                hasProperty("processStatus", is(ProcessStatus.PROCESSED)),
                hasProperty("processStatus", is(ProcessStatus.PROCESSED))
        ));

        // So a download of its packages should be valid
        for (OaiRecord oaiRecord : CrudOperations.getRecords().getResult()) {
            validatePackage(oaiRecord);
        }
    }

    private void validatePackage(OaiRecord oaiRecord) throws IOException {
        System.out.println("Downloading and validating package");
        final ZipInputStream zip = new ZipInputStream(CrudOperations.download(oaiRecord.getIdentifier()));

        ZipEntry entry;
        while((entry = zip.getNextEntry())!=null) {
            System.out.println(entry.getName());
        }

        zip.close();
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

    private Thread getRecordProcessingWaiter() {
        //socketStatus.getStatus().recordProcessingStatus.recordStatus
        return new Thread(() -> {
            boolean processingEncountered = false;
            boolean waitingForRecords = true;
            while (waitingForRecords) {
                final Map<String, Map<String, Long>> recordStatus =
                        socketStatus.getStatus().recordProcessingStatus.recordStatus;

                final Map<String, Long> statusForRepo = recordStatus.getOrDefault("1", Maps.newHashMap());
                if (statusForRepo.containsKey("processing")) {
                    processingEncountered = true;
                } else if (processingEncountered && !statusForRepo.containsKey("pending")) {
                    waitingForRecords = false;
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
                    socketStatus.setStatus(new ObjectMapper().readValue(msg, SocketStatusUpdate.class));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}

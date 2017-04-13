package nl.kb.dare.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import io.dropwizard.testing.junit.DropwizardAppRule;
import nl.kb.dare.App;
import nl.kb.stream.ChecksumOutputStream;
import nl.kb.dare.integration.crud.CrudOperations;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.statuscodes.OaiStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.dare.manifest.ManifestXmlHandler;
import nl.kb.dare.manifest.ObjectResource;
import nl.kb.dare.oai.ScheduledOaiHarvester;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.fail;

public class IntegrationTest {
    private static final String APP_HOST = "localhost:4567";
    public static final String APP_URL = "http://" + APP_HOST;
    private static final String OAI_URL = "http://localhost:18081/oai";

    private static final IntegrationSocketClientStatus socketStatus = new IntegrationSocketClientStatus();

    private static final SAXParser saxParser;

    @ClassRule
    public static final TestRule oaiRule;

    @ClassRule
    public static final TestRule instanceRule;

    static {
        try {
            saxParser = SAXParserFactory.newInstance().newSAXParser();
            oaiRule = new DropwizardAppRule<>(OaiTestServer.class,
                    Paths.get(IntegrationTest.class.getResource("/integration/oai-test-server.yaml").toURI()).toString());
            instanceRule  = new DropwizardAppRule<>(App.class,
                    Paths.get(IntegrationTest.class.getResource("/integration/integration.yaml").toURI()).toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize sax parser", e);
        }
    }

    private StatusClientSocket socket;
    private static Connection connection;


    @AfterClass
    public static void teardown()  {
        cleanFiles();
    }

    private static void cleanFiles() {
        try {
            FileUtils.deleteDirectory(new File("./0"));
            FileUtils.deleteDirectory(new File("./6"));
            FileUtils.deleteDirectory(new File("./1"));

        } catch (IOException ignored) {

        }
    }

    @Before
    public void setUp() throws Exception {
        Class.forName("org.h2.Driver");
        connection = DriverManager.getConnection(
                "jdbc:h2:mem:dareintegration", "daredev", "daredev");

        final Statement statement = connection.createStatement();

        final String schemaSql = IOUtils.toString(IntegrationTest.class.getResourceAsStream("/integration/db/schema.sql"), "UTF8");

        for (String statementSql : schemaSql.split(";")) {
            statement.addBatch(statementSql);
        }

        statement.executeBatch();
        statement.close();

        // Add a websocket client to keep track of progress
        final WebSocketClient webSocketClient = new WebSocketClient();
        socket = new StatusClientSocket();
        webSocketClient.start();
        webSocketClient.connect(socket, new URI(String.format("ws://%s/status-socket", APP_HOST)), new ClientUpgradeRequest());
        cleanFiles();
    }

    @After
    public void tearDown() throws InterruptedException {
        socket.closeSession();
        while (socket.isOpen()) {
            Thread.sleep(5L);
        }
    }

    @Test
    public void runHappyFlow() throws Exception {
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

        // So a download of its packages should be valid
        for (OaiRecord oaiRecord : CrudOperations.getRecords().getResult()) {
            validatePackage(oaiRecord);
        }
    }

    @Test
    public void runUpdatesEncounteredFlow() throws IOException, InterruptedException {
        // First create a new repository configuration via HTTP POST to app url
        final String locationOfNewlyCreatedRepository = CrudOperations.createRepository(new Repository(
                OAI_URL,
                "Integration test OAI",
                "nl_didl_norm",
                "test-updating",
                null,
                false));

        // Next enable it by executing a PUT to the returned location
        if (!CrudOperations.enableRepository(locationOfNewlyCreatedRepository)) {
            fail("failed to enable repository: " + locationOfNewlyCreatedRepository);
        }

        runHarvester();
        runRecordProcessor();
        if (!CrudOperations.stopRecordProcessor()) {
            fail("Failed to stop the record processor");
        }

        runHarvester();

        // Based on the mock responses there should be 4 records pending
        assertThat(CrudOperations.getRecords().getResult(), containsInAnyOrder(
                hasProperty("processStatus", is(ProcessStatus.PROCESSED)),
                hasProperty("processStatus", is(ProcessStatus.PROCESSED)),
                allOf(
                    hasProperty("processStatus", is(ProcessStatus.PENDING)),
                    hasProperty("updateCount", is(1)),
                    hasProperty("dateStamp", is("2017-01-20T01:00:31Z"))
                ),
                allOf(
                    hasProperty("oaiStatus", is(OaiStatus.DELETED)),
                    hasProperty("processStatus", is(ProcessStatus.SKIP)),
                    hasProperty("updateCount", is(1)),
                    hasProperty("dateStamp", is("2017-01-20T01:00:31Z"))
                )
        ));
    }


    private void testInitialHarvest() throws IOException, InterruptedException {
        runHarvester();


        // Based on the mock responses there should be 4 records pending
        assertThat(CrudOperations.getRecords().getResult(), containsInAnyOrder(
                hasProperty("processStatus", is(ProcessStatus.PENDING)),
                hasProperty("processStatus", is(ProcessStatus.PENDING)),
                hasProperty("processStatus", is(ProcessStatus.PENDING)),
                hasProperty("processStatus", is(ProcessStatus.PENDING))
        ));
    }

    private void runHarvester() throws IOException, InterruptedException {
        // Start a waiter thread for the first harvest to finish
        final Thread waitForHarvest = getHarvestWaiter();
        waitForHarvest.start();

        // Then start the harvester (which is disabled by default)
        if (!CrudOperations.startHarvester()) { fail("failed to start the harvester"); }

        // Wait for the first harvest to finish
        waitForHarvest.join();
    }

    private void testFirstRecordProcessingRun() throws IOException, InterruptedException, SAXException {
        runRecordProcessor();


        // Based on the mock responses all 4 records should be succesfully processed
        assertThat(CrudOperations.getRecords().getResult(), containsInAnyOrder(
                hasProperty("processStatus", is(ProcessStatus.PROCESSED)),
                hasProperty("processStatus", is(ProcessStatus.PROCESSED)),
                hasProperty("processStatus", is(ProcessStatus.PROCESSED)),
                hasProperty("processStatus", is(ProcessStatus.PROCESSED))
        ));
    }

    private void runRecordProcessor() throws IOException, InterruptedException {
        // Start a waiter thread for all records to be processed
        final Thread waitForRecordsProcessed = getRecordProcessingWaiter();
        waitForRecordsProcessed.start();

        // Then start the record processor (which is disabled by default)
        if (!CrudOperations.startRecordProcessor()) { fail("failed to start the record processor"); }

        // Wait for all records to be processed
        waitForRecordsProcessed.join();
    }


    private void validatePackage(OaiRecord oaiRecord) throws IOException, SAXException, NoSuchAlgorithmException {
        System.out.println("Downloading and validating package");
        final NonClosingZipInputStream zip = new NonClosingZipInputStream(CrudOperations.download(oaiRecord.getIdentifier()));

        ZipEntry entry;
        List<ObjectResource> objectResourcesInManifest = null;
        Map<String, String> downloadedChecksums = Maps.newHashMap();
        Map<String, Long> fileSizes = Maps.newHashMap();

        while((entry = zip.getNextEntry())!= null) {
            final String filename = entry.getName();
            final ChecksumOutputStream out = new ChecksumOutputStream("SHA-512");
            if (filename.equals("manifest.xml")) {
                final ManifestXmlHandler manifestXmlHandler = new ManifestXmlHandler();
                synchronized (saxParser) {
                    saxParser.parse(zip, manifestXmlHandler);
                }
                objectResourcesInManifest = manifestXmlHandler.getObjectResourcesIncludingMetadata();
            } else {
                final byte[] bytes = IOUtils.toByteArray(zip);
                out.write(bytes);
                downloadedChecksums.put("file://./" + filename, out.getChecksumString());
                fileSizes.put("file://./" + filename, (long) bytes.length);
            }
        }

        zip.closeAfter();

        // Ensure a manifest.xml was encountered in the zip file
        assertThat(objectResourcesInManifest, notNullValue());
        // Ensure a metadata.xml was encountered in the zip file
        assertThat(downloadedChecksums.containsKey("file://./metadata.xml"), is (true));

        assert objectResourcesInManifest != null;

        // Ensure that each file in the manifest is present in the zip
        for (ObjectResource objectResource : objectResourcesInManifest) {
            final boolean isInZipFile = downloadedChecksums.containsKey(objectResource.getXlinkHref());
            assertThat(isInZipFile, is(true));
            System.out.println("File is present: " + objectResource.getXlinkHref());

            // also check the checksum of the downloaded file against the checksum in the manifest
            assertThat(downloadedChecksums.get(objectResource.getXlinkHref()), equalTo(objectResource.getChecksum()));
            System.out.println("Checksums match: " + objectResource.getChecksum());

            assertThat(fileSizes.get(objectResource.getXlinkHref()), equalTo(objectResource.getSize()));
            System.out.println("Filesizes match: " + objectResource.getSize());

        }

        // Ensure that each file in the zip is also present in the manifest
        for (String filename : downloadedChecksums.keySet()) {
            final boolean isMentionedInManifest = objectResourcesInManifest.stream().map(ObjectResource::getXlinkHref)
                    .anyMatch(s -> s.equals(filename));
            assertThat(isMentionedInManifest, is(true));
        }


    }

    private Thread getHarvestWaiter() {
        return new Thread(() -> {

            boolean hasRun = false, isRunning = false;
            while (!hasRun) {
                final SocketStatusUpdate.HarvesterStatus harvesterStatus = socketStatus.getStatus().harvesterStatus;

                if (harvesterStatus.harvesterRunState == ScheduledOaiHarvester.RunState.WAITING
                        && harvesterStatus.nextRunTime > 0) {
                    hasRun = isRunning;
                } else if (harvesterStatus.harvesterRunState == ScheduledOaiHarvester.RunState.RUNNING || (
                        harvesterStatus.harvesterRunState == ScheduledOaiHarvester.RunState.WAITING
                        && harvesterStatus.nextRunTime <= 0)) {
                    isRunning = true;
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

        private Session session;

        @OnWebSocketClose
        public void onClose(int statusCode, String reason) {
            System.out.println("Wesbocket connection closed");
        }

        @OnWebSocketConnect
        public void onConnect(Session session) {
            this.session = session;
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

        void closeSession() {
            session.close();
        }

        boolean isOpen() {
            return session.isOpen();
        }
    }

    private class NonClosingZipInputStream extends ZipInputStream {

        NonClosingZipInputStream(InputStream in) {
            super(in);
        }

        @Override
        public void close() throws IOException {

        }

        void closeAfter() throws IOException {
            super.close();
        }
    }
}

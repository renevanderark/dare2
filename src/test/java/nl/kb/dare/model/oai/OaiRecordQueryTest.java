package nl.kb.dare.model.oai;

import nl.kb.dare.model.reporting.ErrorReportDao;
import nl.kb.dare.model.reporting.OaiRecordErrorReport;
import nl.kb.dare.model.statuscodes.ErrorStatus;
import nl.kb.dare.model.statuscodes.OaiStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class OaiRecordQueryTest {

    private static final String CREATE_TABLE = "CREATE TABLE `oai_records` (\n" +
            "  `identifier` varchar(128) NOT NULL,\n" +
            "  `datestamp` varchar(50) DEFAULT NULL,\n" +
            "  `repository_id` int(11) DEFAULT NULL,\n" +
            "  `oai_status_code` int(11) DEFAULT NULL,\n" +
            "  `process_status_code` int(11) DEFAULT NULL,\n" +
            "  `update_count` int(11) NOT NULL DEFAULT 0)";

    private static final String CREATE_ERROR_TABLE = "CREATE TABLE `oai_record_errors` (\n"+
            "  `record_identifier` varchar(128) DEFAULT NULL,\n"+
            "  `datestamp` varchar(50) DEFAULT NULL,\n"+
            "  `message` varchar(1024) DEFAULT NULL,\n"+
            "  `url` varchar(1024) DEFAULT NULL,\n"+
            "  `stacktrace` text,\n"+
            "  `status_code` int(11) DEFAULT NULL)";

    private final OaiRecordQuery allQ = new OaiRecordQuery(null, null, null, null, null, null);
    private final OaiRecordQuery offsetAndLimitQ = new OaiRecordQuery(null, 1, 2, null, null, null);
    private final OaiRecordQuery forRepoQ = new OaiRecordQuery(1, null, null, null, null, null);
    private final OaiRecordQuery pendingQ = new OaiRecordQuery(null, null, null, ProcessStatus.PENDING, null, null);
    private final OaiRecordQuery deletedQ = new OaiRecordQuery(null, null, null, null, OaiStatus.DELETED, null);
    private final OaiRecordQuery combinedQ = new OaiRecordQuery(null, null, null, ProcessStatus.PROCESSED, OaiStatus.AVAILABLE, null);
    private final OaiRecordQuery errorQ1 = new OaiRecordQuery(null, null, null, ProcessStatus.FAILED, null, ErrorStatus.NOT_FOUND);
    private final OaiRecordQuery errorQ2 = new OaiRecordQuery(null, null, null, ProcessStatus.FAILED, null, ErrorStatus.INTERNAL_SERVER_ERROR);

    private JdbcConnectionPool dataSource;
    private OaiRecordDao oaiRecordDao;
    private Handle handle;
    private DBI dbi;
    private ErrorReportDao errorReportDao;

    @Before
    public void setup() {
        dataSource = JdbcConnectionPool.create("jdbc:h2:mem:test", "username", "password");
        dbi = new DBI(dataSource);
        handle = dbi.open();
        handle.execute(CREATE_TABLE);
        handle.execute(CREATE_ERROR_TABLE);
        oaiRecordDao = dbi.onDemand(OaiRecordDao.class);
        errorReportDao = dbi.onDemand(ErrorReportDao.class);
        prepareData();
    }

    @After
    public void tearDown() {
        handle.close();
        dataSource.dispose();
    }

    @Test
    public void getCountShouldReturnTheAmountOfRecords() {

        final Long countAll = allQ.getCount(dbi);
        final Long countRepoOne = forRepoQ.getCount(dbi);
        final Long countPending = pendingQ.getCount(dbi);
        final Long countDeleted = deletedQ.getCount(dbi);
        final Long countCombined = combinedQ.getCount(dbi);
        final Long countForErrors1 = errorQ1.getCount(dbi);
        final Long countForErrors2 = errorQ2.getCount(dbi);


        assertThat(countAll, is(6L));
        assertThat(countRepoOne, is(2L));
        assertThat(countPending, is(2L));
        assertThat(countDeleted, is(2L));
        assertThat(countCombined, is(1L));
        assertThat(countForErrors1, is(1L));
        assertThat(countForErrors2, is(1L));
    }

    @Test
    public void getResultShouldReturnTheRecords() {

        final List<OaiRecord> all = allQ.getResults(dbi);
        final List<OaiRecord> withOffsetAndLimit = offsetAndLimitQ.getResults(dbi);
        final List<OaiRecord> forRepo = forRepoQ.getResults(dbi);
        final List<OaiRecord> pending = pendingQ.getResults(dbi);
        final List<OaiRecord> deleted = deletedQ.getResults(dbi);
        final List<OaiRecord> combined = combinedQ.getResults(dbi);
        final List<OaiRecord> forError1 = errorQ1.getResults(dbi);
        final List<OaiRecord> forError2 = errorQ2.getResults(dbi);

        assertThat(all.size(), is(6));
        assertThat(withOffsetAndLimit.size(), is(2));
        assertThat(forRepo.size(), is(2));
        assertThat(pending.size(), is(2));
        assertThat(deleted.size(), is(2));
        assertThat(combined.size(), is(1));
        assertThat(forError1.size(), is(1));
        assertThat(forError2.size(), is(1));
    }

    @Test
    public void resetToPendingShouldResetTheSelectionToPending() {

        combinedQ.resetToPending(dbi);
        assertThat(pendingQ.getCount(dbi), is(3L));

        errorQ1.resetToPending(dbi);
        assertThat(pendingQ.getCount(dbi), is(4L));
    }


    private void prepareData() {
        Stream.of(1, 1, 2, 4, 2, 3).map(repoId -> new OaiRecord(
                UUID.randomUUID().toString(),
                "2017-01-01T00:00:00Z",
                generateOaiStatus(repoId),
                repoId,
                generateProcessStatus(repoId)
        )).forEach(oaiRecord -> {
            oaiRecordDao.insert(oaiRecord);
            if (oaiRecord.getProcessStatus() == ProcessStatus.FAILED) {
                errorReportDao.insertOaiRecordError(new OaiRecordErrorReport(
                        "message 1", "", "", "", ErrorStatus.INTERNAL_SERVER_ERROR, oaiRecord.getIdentifier()
                ));
                errorReportDao.insertOaiRecordError(new OaiRecordErrorReport(
                        "message 2", "", "", "", ErrorStatus.NOT_FOUND, oaiRecord.getIdentifier()
                ));
            }
        });
    }

    private OaiStatus generateOaiStatus(Integer repoId) {
        switch (repoId) {
            case 2:
                return OaiStatus.DELETED;
            default:
                return OaiStatus.AVAILABLE;
        }
    }

    private ProcessStatus generateProcessStatus(Integer repoId) {
        switch (repoId) {
            case 2:
                return ProcessStatus.SKIP;
            case 3:
                return ProcessStatus.PROCESSED;
            case 4:
                return ProcessStatus.FAILED;
            default:
                return ProcessStatus.PENDING;
        }

    }
}
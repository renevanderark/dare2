package nl.kb.dare.model.reporting;

import nl.kb.dare.model.statuscodes.ErrorStatus;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

public class ErrorReportDaoTest {

    private static final String CREATE_OAI_RECORD_ERROR_TABLE = "CREATE TABLE `oai_record_errors` (\n" +
            "  `record_identifier` varchar(128) DEFAULT NULL,\n" +
            "  `datestamp` varchar(50) DEFAULT NULL,\n" +
            "  `message` varchar(255) DEFAULT NULL,\n" +
            "  `url` varchar(255) DEFAULT NULL,\n" +
            "  `stacktrace` text,\n" +
            "  `status_code` int(11) DEFAULT NULL)";
    private static final String CREATE_HARVESTER_ERROR_TABLE = "CREATE TABLE `harvester_errors` (\n" +
            "  `repository_id` int(11) DEFAULT NULL,\n" +
            "  `url` varchar(255) DEFAULT NULL,\n" +
            "  `message` varchar(255) DEFAULT NULL,\n" +
            "  `stacktrace` text,\n" +
            "  `datestamp` varchar(50) DEFAULT NULL,\n" +
            "  `status_code` int(11) DEFAULT NULL)";

    private JdbcConnectionPool dataSource;
    private Handle handle;
    private ErrorReportDao errorReportDao;

    @Before
    public void setup() {
        dataSource = JdbcConnectionPool.create("jdbc:h2:mem:test", "username", "password");
        final DBI dbi = new DBI(dataSource);
        handle = dbi.open();
        handle.execute(CREATE_OAI_RECORD_ERROR_TABLE);
        handle.execute(CREATE_HARVESTER_ERROR_TABLE);
        errorReportDao = dbi.onDemand(ErrorReportDao.class);
    }

    @After
    public void tearDown() {
        handle.close();
        dataSource.dispose();
    }

    @Test
    public void insertHarvesterErrorShouldSetTheCorrectFields() throws MalformedURLException {
        try {
            throw new IllegalArgumentException("testing");
        } catch (IllegalArgumentException e) {
            final ErrorReport errorReport = new ErrorReport(e, new URL("http://example.com"), ErrorStatus.INTERNAL_SERVER_ERROR);
            errorReportDao.insertHarvesterError(new HarvesterErrorReport(errorReport, 123));

            final Map<String, Object> result = handle.createQuery("select * from harvester_errors limit 1").first();

            assertThat(result.get("repository_id"), is(123));
            assertThat(result.get("message"), is("testing"));
            assertThat(result.get("status_code"), is(ErrorStatus.INTERNAL_SERVER_ERROR.getCode()));
            assertThat(result.get("url"), is("http://example.com"));
            assertThat(result.get("stacktrace"), is(notNullValue()));
            assertThat(result.get("datestamp"),  is(notNullValue()));
        }
    }

    @Test
    public void insertOaiRecordErrorShouldSetTheCorrectFields() throws MalformedURLException, SQLException {
        try {
            throw new IllegalArgumentException("testing");
        } catch (IllegalArgumentException e) {
            errorReportDao.insertOaiRecordError(new OaiRecordErrorReport(
                    "testing",
                    "trace",
                    "date",
                    "http://example.com",
                    ErrorStatus.INTERNAL_SERVER_ERROR,
                    "id"
            ));

            final Map<String, Object> result = handle.createQuery("select * from oai_record_errors limit 1").first();

            assertThat(result.get("record_identifier"), is("id"));
            assertThat(result.get("message"), is("testing"));
            assertThat(result.get("status_code"), is(ErrorStatus.INTERNAL_SERVER_ERROR.getCode()));
            assertThat(result.get("url"), is("http://example.com"));
            final Clob stacktrace = (Clob) result.get("stacktrace");
            assertThat(stacktrace.getSubString(1, (int) stacktrace.length()), is("trace"));
            assertThat(result.get("datestamp"),  is("date"));
        }
    }
}
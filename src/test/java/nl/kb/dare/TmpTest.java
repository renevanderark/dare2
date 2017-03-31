package nl.kb.dare;

import com.google.common.collect.Lists;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.oai.OaiRecordDao;
import nl.kb.dare.model.statuscodes.OaiStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

public class TmpTest {
    private static final String CREATE_TABLE = "CREATE TABLE oai_records (" +
            "  identifier varchar(128) NOT NULL)";

    private OaiRecordDao oaiRecordDao;
    private Handle handle;

    @Before
    public void setup() throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        final Connection connection = DriverManager.getConnection("jdbc:oracle:thin:system/oracle@localhost:49161:xe");


        final String schemaSql = IOUtils.toString(TmpTest.class.getResourceAsStream("/tmp/tmp.sql"), "UTF8");



        StringBuilder sb = new StringBuilder();
        for (String line : schemaSql.split("\n")) {
            if (line.trim().length() == 0) {
                final String sql = sb.toString();
                try (Statement statement = connection.createStatement()) {
                    statement.execute(sql);
                } catch (SQLException e) {
                    System.err.println(sql);
                    e.printStackTrace();
                }
                sb = new StringBuilder();
            } else {
                sb.append(line).append("\n");
            }

        }

        final DBI dbi = new DBI("jdbc:oracle:thin:system/oracle@localhost:49161:xe");
        handle = dbi.open();
        oaiRecordDao = dbi.onDemand(OaiRecordDao.class);
    }



    @After
    public void tearDown() {
        handle.close();
    }


    @Test
    public void insertShouldSetTheOaiRecordFieldsCorrectly() {
        final OaiRecord oaiRecord = new OaiRecord(
                "identifier:id",
                "2017-01-01T00:00:00Z",
                OaiStatus.AVAILABLE,
                123,
                ProcessStatus.PENDING
        );
        oaiRecordDao.insert(oaiRecord);

        final Map<String, Object> result = handle.createQuery("select * from oai_records where rownum <= 1").first();

        assertThat(result.get("identifier"), is("identifier:id"));
        assertThat(result.get("datestamp"), is("2017-01-01T00:00:00Z"));
        assertThat(((BigDecimal) result.get("oai_status_code")).intValue(), is(OaiStatus.AVAILABLE.getCode()));
        assertThat(((BigDecimal) result.get("repository_id")).intValue(), is(123));
        assertThat(((BigDecimal) result.get("process_status_code")).intValue(), is(ProcessStatus.PENDING.getCode()));
    }

    @Test
    public void deleteForRepositoryShouldRemoveAllRecordsForTheRepositoryId() {
        Stream.of(1, 1, 2, 1, 2, 3).map(repoId -> new OaiRecord(
                UUID.randomUUID().toString(),
                "2017-01-01T00:00:00Z",
                OaiStatus.AVAILABLE,
                repoId,
                ProcessStatus.PENDING
        )).forEach(oaiRecordDao::insert);

        oaiRecordDao.removeForRepository(1);

        final List<BigDecimal> repoIds = Lists.newArrayList();
        for (Map<String, Object> result : handle.createQuery("select repository_id from oai_records")) {
            repoIds.add((BigDecimal) result.get("repository_id"));
        }

        assertThat(repoIds, not(hasItem(new BigDecimal(1))));
        assertThat(repoIds, containsInAnyOrder(new BigDecimal(2), new BigDecimal(2), new BigDecimal(3)));
    }

    @Test
    public void findAllForRepositoryShouldReturnAllRecordsForTheRepositoryId() {
        Stream.of(1, 1, 2, 3, 2, 1).map(repoId -> new OaiRecord(
                repoId == 3 ? "find-me" : UUID.randomUUID().toString(),
                "2017-01-01T00:00:00Z",
                OaiStatus.AVAILABLE,
                repoId,
                ProcessStatus.PENDING
        )).forEach(oaiRecordDao::insert);

        final Iterator<String> result = oaiRecordDao.findAllForRepository(3);

        assertThat(result.hasNext(), is(true));
        assertThat(result.next(), is("find-me"));
        assertThat(result.hasNext(), is(false));
    }

    @Test
    public void updateShouldSetTheOaiRecordFieldsCorrectly() {
        final OaiRecord oaiRecord1 = new OaiRecord(
                "identifier:id:1",
                "2017-01-01T00:00:00Z",
                OaiStatus.AVAILABLE,
                123,
                ProcessStatus.PENDING
        );
        final OaiRecord oaiRecord2 = new OaiRecord(
                "identifier:id:1",
                "2017-01-01T00:00:01Z",
                OaiStatus.DELETED,
                123,
                ProcessStatus.SKIP
        );

        oaiRecordDao.insert(oaiRecord1);
        oaiRecordDao.update(oaiRecord2);
        final Map<String, Object> first = handle.createQuery("select * from oai_records where rownum <= 1").first();

        assertThat(first.get("identifier"), is("identifier:id:1"));
        assertThat(first.get("datestamp"), is("2017-01-01T00:00:01Z"));
        assertThat(((BigDecimal) first.get("oai_status_code")).intValue(), is(OaiStatus.DELETED.getCode()));
        assertThat(((BigDecimal) first.get("repository_id")).intValue(), is(123));
        assertThat(((BigDecimal) first.get("process_status_code")).intValue(), is(ProcessStatus.SKIP.getCode()));
    }

    @Test
    public void findByIdentifierShouldReturnTheCorrectOaiRecord() {
        final OaiRecord oaiRecord = new OaiRecord(
                "identifier:id:2",
                "2017-01-01T00:00:00Z",
                OaiStatus.AVAILABLE,
                123,
                ProcessStatus.PENDING
        );

        oaiRecordDao.insert(oaiRecord);
        final OaiRecord returnedOaiRecord = oaiRecordDao.findByIdentifier(oaiRecord.getIdentifier());

        assertThat(oaiRecord, equalTo(returnedOaiRecord));
    }



}

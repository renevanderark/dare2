package nl.kb.dare.model.oai;

import com.google.common.collect.Lists;
import nl.kb.oaipmh.OaiStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

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

public class OaiRecordDaoTest {

    private static final String CREATE_TABLE = "CREATE TABLE `oai_records` (\n" +
            "  `identifier` varchar(128) NOT NULL,\n" +
            "  `datestamp` varchar(50) DEFAULT NULL,\n" +
            "  `repository_id` int(11) DEFAULT NULL,\n" +
            "  `oai_status_code` int(11) DEFAULT NULL,\n" +
            "  `process_status_code` int(11) DEFAULT NULL,\n" +
            "  `total_file_size` bigint(20) DEFAULT NULL,\n" +
            "  `update_count` int(11) NOT NULL DEFAULT 0)";

    private JdbcConnectionPool dataSource;
    private OaiRecordDao oaiRecordDao;
    private Handle handle;

    @Before
    public void setup() {
        dataSource = JdbcConnectionPool.create("jdbc:h2:mem:test", "username", "password");
        final DBI dbi = new DBI(dataSource);
        handle = dbi.open();
        handle.execute(CREATE_TABLE);
        oaiRecordDao = dbi.onDemand(OaiRecordDao.class);
    }

    @After
    public void tearDown() {
        handle.close();
        dataSource.dispose();
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

        final Map<String, Object> result = handle.createQuery("select * from oai_records limit 1").first();

        assertThat(result.get("identifier"), is("identifier:id"));
        assertThat(result.get("datestamp"), is("2017-01-01T00:00:00Z"));
        assertThat(result.get("oai_status_code"), is(OaiStatus.AVAILABLE.getCode()));
        assertThat(result.get("repository_id"), is(123));
        assertThat(result.get("process_status_code"), is(ProcessStatus.PENDING.getCode()));
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

        final List<Integer> repoIds = Lists.newArrayList();
        for (Map<String, Object> result : handle.createQuery("select repository_id from oai_records")) {
            repoIds.add((Integer) result.get("repository_id"));
        }

        assertThat(repoIds, not(hasItem(1)));
        assertThat(repoIds, containsInAnyOrder(2, 2, 3));
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
                "identifier:id",
                "2017-01-01T00:00:00Z",
                OaiStatus.AVAILABLE,
                123,
                ProcessStatus.PENDING
        );
        final OaiRecord oaiRecord2 = new OaiRecord(
                "identifier:id",
                "2017-01-01T00:00:01Z",
                OaiStatus.DELETED,
                123,
                ProcessStatus.SKIP
        );

        oaiRecordDao.insert(oaiRecord1);
        oaiRecordDao.update(oaiRecord2);
        final Map<String, Object> first = handle.createQuery("select * from oai_records limit 1").first();

        assertThat(first.get("identifier"), is("identifier:id"));
        assertThat(first.get("datestamp"), is("2017-01-01T00:00:01Z"));
        assertThat(first.get("oai_status_code"), is(OaiStatus.DELETED.getCode()));
        assertThat(first.get("repository_id"), is(123));
        assertThat(first.get("process_status_code"), is(ProcessStatus.SKIP.getCode()));
    }

    @Test
    public void findByIdentifierShouldReturnTheCorrectOaiRecord() {
        final OaiRecord oaiRecord = new OaiRecord(
                "identifier:id",
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
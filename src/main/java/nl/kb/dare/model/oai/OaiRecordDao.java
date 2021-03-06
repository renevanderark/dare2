package nl.kb.dare.model.oai;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.Iterator;
import java.util.List;

@RegisterMapper(OaiRecordMapper.class)
public interface OaiRecordDao {

    @SqlQuery("select * from oai_records where identifier = :identifier")
    OaiRecord findByIdentifier(@Bind("identifier") String identifier);

    @SqlQuery("select * from oai_records where process_status_code = :process_status_code AND repository_id = :repository_id LIMIT :limit")
    List<OaiRecord> fetchNextWithProcessStatusByRepositoryId(
            @Bind("process_status_code") Integer processStatusCode,
            @Bind("limit") Integer limit,
            @Bind("repository_id") Integer repositoryId);

    @SqlUpdate("insert into oai_records (identifier, datestamp, oai_status_code, repository_id, process_status_code) " +
            "values (:oaiRecord.identifier, :oaiRecord.dateStamp, :oaiRecord.oaiStatusCode, :oaiRecord.repositoryId, :oaiRecord.processStatusCode)")
    void insert(@BindBean("oaiRecord") OaiRecord oaiRecord);

    @SqlUpdate("update oai_records " +
            "set datestamp = :oaiRecord.dateStamp, " +
            "   oai_status_code = :oaiRecord.oaiStatusCode, " +
            "   repository_id = :oaiRecord.repositoryId, " +
            "   process_status_code = :oaiRecord.processStatusCode, " +
            "   update_count = :oaiRecord.updateCount, " +
            "   total_file_size = :oaiRecord.totalFileSize " +
            "where identifier = :oaiRecord.identifier")
    void update(@BindBean("oaiRecord") OaiRecord oaiRecord);

    @SqlUpdate("delete from oai_records where repository_id = :repositoryId")
    void removeForRepository(@Bind("repositoryId") Integer repositoryId);

    @SqlQuery("select identifier from oai_records where repository_id = :repositoryId")
    Iterator<String> findAllForRepository(@Bind("repositoryId") Integer repositoryId);


}

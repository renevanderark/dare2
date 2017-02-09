package nl.kb.dare.model.oai;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(OaiRecordMapper.class)
public interface OaiRecordDao {

    @SqlQuery("select * from oai_records where identifier = :identifier")
    OaiRecord findByIdentifier(@Bind("identifier") String identifier);

    @SqlUpdate("insert into oai_records (identifier, datestamp, oai_status_code, repository_id, process_status_code) " +
            "values (:oaiRecord.identifier, :oaiRecord.dateStamp, :oaiRecord.oaiStatusCode, :oaiRecord.repositoryId, :oaiRecord.processStatusCode)")
    void insert(@BindBean("oaiRecord") OaiRecord oaiRecord);

    @SqlUpdate("update oai_records " +
            "set datestamp = :oaiRecord.dateStamp, oai_status_code = :oaiRecord.oaiStatusCode, repository_id = :oaiRecord.repositoryId " +
            "where identifier = :oaiRecord.identifier")
    void update(@BindBean("oaiRecord") OaiRecord oaiRecord);

    @SqlQuery("select * from oai_records where repository_id = :repositoryId limit :limit offset :offset")
    List<OaiRecord> list(
            @Bind("repositoryId") Integer repositoryId,
            @Bind("offset") Integer offset,
            @Bind("limit") Integer limit);

    @SqlQuery("select * from oai_records " +
            "where repository_id = :repositoryId and oai_status_code = :oai_status_code " +
            "limit :limit offset :offset")
    List<OaiRecord> listWithOaiStatus(
            @Bind("repositoryId") Integer repositoryId,
            @Bind("offset") Integer offset,
            @Bind("limit") Integer limit,
            @Bind("oai_status_code") Integer oaiStatusCode);

    @SqlQuery("select * from oai_records " +
            "where repository_id = :repositoryId and process_status_code = :process_status_code " +
            "limit :limit offset :offset")
    List<OaiRecord> listWithProcessStatus(
            @Bind("repositoryId") Integer repositoryId,
            @Bind("offset") Integer offset,
            @Bind("limit") Integer limit,
            @Bind("process_status_code") Integer processStatusCode);

    @SqlQuery("select * from oai_records " +
            "where repository_id = :repositoryId and process_status_code = :process_status_code and oai_status_code = :oai_status_code " +
            "limit :limit offset :offset")
    List<OaiRecord> listWithProcessStatusAndOaiStatus(
            @Bind("repositoryId") Integer repositoryId,
            @Bind("offset") Integer offset,
            @Bind("limit") Integer limit,
            @Bind("process_status_code") Integer processStatusCode,
            @Bind("oai_status_code") Integer oaiStatusCode);


    @SqlQuery("select count(*) from oai_records where repository_id = :repositoryId")
    Long count(@Bind("repositoryId") Integer repositoryId);

    @SqlQuery("select count(*) from oai_records " +
            "where repository_id = :repositoryId and oai_status_code = :oai_status_code ")
    Long countWithOaiStatus(
            @Bind("repositoryId") Integer repositoryId,
            @Bind("oai_status_code") Integer oaiStatusCode);

    @SqlQuery("select count(*) from oai_records " +
            "where repository_id = :repositoryId and process_status_code = :process_status_code ")
    Long countWithProcessStatus(
            @Bind("repositoryId") Integer repositoryId,
            @Bind("process_status_code") Integer processStatusCode);

    @SqlQuery("select count(*) from oai_records " +
            "where repository_id = :repositoryId and process_status_code = :process_status_code and oai_status_code = :oai_status_code ")
    Long countWithProcessStatusAndOaiStatus(
            @Bind("repositoryId") Integer repositoryId,
            @Bind("process_status_code") Integer processStatusCode,
            @Bind("oai_status_code") Integer oaiStatusCode);
}

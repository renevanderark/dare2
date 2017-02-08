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

    @SqlUpdate("insert into oai_records (identifier, datestamp, oai_status, repository_id, process_status) " +
            "values (:oaiRecord.identifier, :oaiRecord.dateStamp, :oaiRecord.oaiStatus, :oaiRecord.repositoryId, :oaiRecord.processStatus)")
    void insert(@BindBean("oaiRecord") OaiRecord oaiRecord);

    @SqlUpdate("update oai_records " +
            "set datestamp = :oaiRecord.dateStamp, oai_status = :oaiRecord.oaiStatus, repository_id = :oaiRecord.repositoryId " +
            "where identifier = :oaiRecord.identifier")
    void update(@BindBean("oaiRecord") OaiRecord oaiRecord);

    @SqlQuery("select * from oai_records where repository_id = :repositoryId limit :limit offset :offset")
    List<OaiRecord> list(
            @Bind("repositoryId") Integer repositoryId,
            @Bind("offset") Integer offset,
            @Bind("limit") Integer limit);

    @SqlQuery("select * from oai_records " +
            "where repository_id = :repositoryId and oai_status = :oai_status " +
            "limit :limit offset :offset")
    List<OaiRecord> listWithOaiStatus(
            @Bind("repositoryId") Integer repositoryId,
            @Bind("offset") Integer offset,
            @Bind("limit") Integer limit,
            @Bind("oai_status") String oaiStatus);

    @SqlQuery("select * from oai_records " +
            "where repository_id = :repositoryId and process_status = :process_status " +
            "limit :limit offset :offset")
    List<OaiRecord> listWithProcessStatus(
            @Bind("repositoryId") Integer repositoryId,
            @Bind("offset") Integer offset,
            @Bind("limit") Integer limit,
            @Bind("process_status") String processStatus);

    @SqlQuery("select * from oai_records " +
            "where repository_id = :repositoryId and process_status = :process_status and oai_status = :oai_status " +
            "limit :limit offset :offset")
    List<OaiRecord> listWithProcessStatusAndOaiStatus(
            @Bind("repositoryId") Integer repositoryId,
            @Bind("offset") Integer offset,
            @Bind("limit") Integer limit,
            @Bind("process_status") String processStatus,
            @Bind("oai_status") String oaiStatus);


    @SqlQuery("select count(*) from oai_records where repository_id = :repositoryId")
    Long count(
            @Bind("repositoryId") Integer repositoryId);

    @SqlQuery("select count(*) from oai_records " +
            "where repository_id = :repositoryId and oai_status = :oai_status ")
    Long countWithOaiStatus(
            @Bind("repositoryId") Integer repositoryId,
            @Bind("oai_status") String oaiStatus);

    @SqlQuery("select count(*) from oai_records " +
            "where repository_id = :repositoryId and process_status = :process_status ")
    Long countWithProcessStatus(
            @Bind("repositoryId") Integer repositoryId,
            @Bind("process_status") String processStatus);

    @SqlQuery("select count(*) from oai_records " +
            "where repository_id = :repositoryId and process_status = :process_status and oai_status = :oai_status ")
    Long countWithProcessStatusAndOaiStatus(
            @Bind("repositoryId") Integer repositoryId,
            @Bind("process_status") String processStatus,
            @Bind("oai_status") String oaiStatus);
}

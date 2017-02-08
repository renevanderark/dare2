package nl.kb.dare.model.oai;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(OaiRecordMapper.class)
public interface OaiRecordDao {

    @SqlQuery("select * from oai_records where identifier = :identifier")
    OaiRecord findByIdentifier(@Bind("identifier") String identifier);

    @SqlUpdate("insert into oai_records (identifier, datestamp, oai_status, repository_id, process_status) " +
            "values (:oaiRecord.identifier, :oaiRecord.dateStamp, :oaiRecord.oaiStatus, :oaiRecord.repositoryId, :oaiRecord.processStatus)")
    void insert(@BindBean("oaiRecord") OaiRecord oaiRecord);

    @SqlUpdate("update oai_records " +
            "set datestamp = :oaiRecord.dateStamp, oai_status = :oaiRecord.oaiStatus, repository_id = :oaiRecord.repositoryId, :oaiRecord.processStatus " +
            "where identifier = :oaiRecord.identifier")
    void update(@BindBean("oaiRecord") OaiRecord oaiRecord);
}

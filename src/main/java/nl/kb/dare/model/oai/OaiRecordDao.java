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

    @SqlUpdate("insert into oai_records (identifier, datestamp, status, repository_id) " +
            "values (:oaiRecord.identifier, :oaiRecord.dateStamp, :oaiRecord.status, :oaiRecord.repositoryId)")
    void insert(@BindBean("oaiRecord") OaiRecord oaiRecord);

    @SqlUpdate("update oai_records " +
            "set datestamp = :oaiRecord.dateStamp, status = :oaiRecord.status, repository_id = :oaiRecord.repositoryId " +
            "where identifier = :oaiRecord.identifier")
    void update(@BindBean("oaiRecord") OaiRecord oaiRecord);
}

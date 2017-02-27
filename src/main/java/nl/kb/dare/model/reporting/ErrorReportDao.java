package nl.kb.dare.model.reporting;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import java.util.List;


public interface ErrorReportDao {

   @SqlUpdate("insert into harvester_errors (repository_id, url, message, stacktrace, datestamp, status_code) " +
            "values (:repositoryId, :url, :message, :filteredStackTrace, :dateStamp, :errorStatusCode)")
    void insertHarvesterError(@BindBean HarvesterErrorReport harvesterErrorReport);


    @SqlUpdate("insert into oai_record_errors (record_identifier, url, message, stacktrace, datestamp, status_code) " +
            "values (:recordIdentifier, :url, :message, :filteredStackTrace, :dateStamp, :errorStatusCode)")
    void insertOaiRecordError(@BindBean OaiRecordErrorReport oaiRecordErrorReport);

    @SqlQuery("select * from oai_record_errors where record_identifier = :recordIdentifier")
    @Mapper(OaiRecordErrorReportMapper.class)
    List<OaiRecordErrorReport> findByRecordIdentifier(@Bind("recordIdentifier") String recordIdentifier);

    @SqlUpdate("delete from oai_record_errors where record_identifier = :recordIdentifier")
    void removeForOaiRecord(@Bind("recordIdentifier") String recordIdentifier);
}

package nl.kb.dare.model.reporting;

import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface ErrorReportDao {

   @SqlUpdate("insert into harvester_errors (repository_id, url, message, stacktrace, datestamp) " +
            "values (:repositoryId, :url, :message, :filteredStackTrace, :dateStamp)")
    void insertHarvesterError(@BindBean HarvesterErrorReport harvesterErrorReport);


    @SqlUpdate("insert into oai_record_errors (record_identifier, url, message, stacktrace, datestamp) " +
            "values (:recordIdentifier, :url, :message, :filteredStackTrace, :dateStamp)")
    void insertOaiRecordError(@BindBean OaiRecordErrorReport oaiRecordErrorReport);

}

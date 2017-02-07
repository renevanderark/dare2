package nl.kb.dare.model.reporting;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface ErrorReportDao {

    @SqlUpdate("delete from harvester_errors where repository_id = :repository_id")
    void removeHarvesterErrorsForRepositoryWithId(@Bind("repository_id") int repositoryId);

   @SqlUpdate("insert into harvester_errors (repository_id, url, message, stacktrace, datestamp) " +
            "values (:repositoryId, :url, :message, :filteredStackTrace, :dateStamp)")
    void insertHarvesterError(@BindBean HarvesterErrorReport harvesterErrorReport);

}

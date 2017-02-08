package nl.kb.dare.model.oai;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OaiRecordMapper implements ResultSetMapper<OaiRecord> {

    @Override
    public OaiRecord map(int index, ResultSet resultSet, StatementContext ctx) throws SQLException {

        final String identifier = resultSet.getString("identifier");
        final String oaiStatus = resultSet.getString("oai_status");
        final String dateStamp = resultSet.getString("datestamp");
        final Integer repositoryId = resultSet.getInt("repository_id");
        final String processStatus = resultSet.getString("process_status");

        return new OaiRecord(identifier, dateStamp, oaiStatus, repositoryId, processStatus);

    }
}

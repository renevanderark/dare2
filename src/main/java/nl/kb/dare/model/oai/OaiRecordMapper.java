package nl.kb.dare.model.oai;

import nl.kb.oaipmh.OaiStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OaiRecordMapper implements ResultSetMapper<OaiRecord> {

    @Override
    public OaiRecord map(int index, ResultSet resultSet, StatementContext ctx) throws SQLException {

        final String identifier = resultSet.getString("identifier");
        final Integer oaiStatusCode = resultSet.getInt("oai_status_code");
        final String dateStamp = resultSet.getString("datestamp");
        final Integer repositoryId = resultSet.getInt("repository_id");
        final Integer processStatusCode = resultSet.getInt("process_status_code");
        final Integer updateCount = resultSet.getInt("update_count");

        return new OaiRecord(identifier, dateStamp, OaiStatus.forCode(oaiStatusCode), repositoryId,
                ProcessStatus.forCode(processStatusCode), updateCount);

    }
}

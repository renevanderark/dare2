package nl.kb.dare.model.reporting;

import nl.kb.dare.model.statuscodes.ErrorStatus;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OaiRecordErrorReportMapper implements ResultSetMapper<OaiRecordErrorReport> {

    @Override
    public OaiRecordErrorReport map(int index, ResultSet resultSet, StatementContext ctx) throws SQLException {

        final String message = resultSet.getString("message");
        final String dateStamp = resultSet.getString("datestamp");
        final String url = resultSet.getString("url");
        final ErrorStatus errorStatus = ErrorStatus.forCode(resultSet.getInt("status_code"));
        final String recordIdentifier = resultSet.getString("record_identifier");
        final Blob stacktrace = resultSet.getBlob("stacktrace");

        final String filteredStackTrace = stacktrace == null ? "" :
                new String(stacktrace.getBytes(1L, (int) stacktrace.length()));

        return new OaiRecordErrorReport(
                message, filteredStackTrace,
                dateStamp, url, errorStatus,
                recordIdentifier
        );
    }
}

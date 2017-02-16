package nl.kb.dare.model.oai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Maps;
import nl.kb.dare.model.statuscodes.ErrorStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.util.Map;
public class OaiRecordStatusAggregator {

    private static final String RECORDS_QUERY =
            "select count(*) as count, " +
                    "oai_records.process_status_code as status_code, " +
                    "repositories.oai_set as oai_set " +
            "from oai_records, repositories " +
            "where repositories.id = oai_records.repository_id " +
            "group by repository_id, process_status_code";

    private static final String ERROR_QUERY =
            "select count(*) as count," +
                    "repositories.oai_set as oai_set, " +
                    "oai_record_errors.status_code as status_code " +
            "from oai_record_errors, oai_records, repositories " +
            "where oai_records.identifier = oai_record_errors.record_identifier " +
            "and repositories.id = oai_records.repository_id " +
            "group by repository_id, oai_record_errors.status_code";

    private final DBI db;

    public OaiRecordStatusAggregator(DBI db) {
        this.db = db;
    }

    public Map<String, Map<String, Map<String, Object>>> getStatus() throws JsonProcessingException {
        final Map<String, Map<String, Map<String, Object>>> resultMap = Maps.newHashMap();

        final Map<String, Map<String, Object>> recordsMap = getAggregation(RECORDS_QUERY, true);
        final Map<String, Map<String, Object>> errorsMap = getAggregation(ERROR_QUERY, false);

        resultMap.put("recordStatus", recordsMap);
        resultMap.put("errorStatus", errorsMap);

        return resultMap;
    }



    private Map<String, Map<String, Object>> getAggregation(String sql, boolean forRecords) {
        final Map<String, Map<String, Object>> result = Maps.newHashMap();
        final Handle handle = db.open();
        for (Map<String, Object> row : handle.createQuery(sql)) {
            final String oaiSet = (String) row.get("oai_set");
            final Integer statusCode = (Integer) row.get("status_code");

            final Map<String, Object> statusMap = result.getOrDefault(oaiSet, Maps.newHashMap());
            if (forRecords) {
                final ProcessStatus processStatus = ProcessStatus.forCode(statusCode);
                if (processStatus != null) {
                    statusMap.put(processStatus.getStatus(), row.get("count"));
                }
            } else {
                final ErrorStatus errorStatus = ErrorStatus.forCode(statusCode);
                if (errorStatus != null) {
                    statusMap.put(errorStatus.getCode() + " - " + errorStatus.getStatus(), row.get("count"));
                }

            }
            result.put(oaiSet, statusMap);
        }
        handle.close();
        return result;
    }
}

package nl.kb.dare.model.oai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.util.Map;
public class OaiRecordStatusAggregator {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String AGGREGATION_QUERY =
            "select count(*) as count, " +
                    "oai_records.process_status_code as process_status_code, " +
                    "repositories.oai_set as oai_set," +
                    "repositories.id as repository_id " +
            "from oai_records, repositories " +
            "where repositories.id = oai_records.repository_id " +
            "group by repository_id, process_status_code";

    private final DBI db;

    public OaiRecordStatusAggregator(DBI db) {
        this.db = db;
    }

    public String getStatus() throws JsonProcessingException {
        final Map<String, Map<String, Object>> resultMap = Maps.newHashMap();
        final Handle handle = db.open();

        for (Map<String, Object> row : handle.createQuery(AGGREGATION_QUERY)) {
            final String oaiSet = (String) row.get("oai_set");
            final Integer processStatusCode = (Integer) row.get("process_status_code");

            final Map<String, Object> statusMap = resultMap.getOrDefault(oaiSet, Maps.newHashMap());
            final ProcessStatus processStatus = ProcessStatus.forCode(processStatusCode);
            if (processStatus != null) {
                statusMap.put(processStatus.getStatus(), row.get("count"));
            }
            statusMap.putIfAbsent("detail", String.format("/repositories/%d/records", (Integer) row.get("repository_id")));
            resultMap.put(oaiSet, statusMap);
        }

        handle.close();
        return OBJECT_MAPPER.writeValueAsString(resultMap);
    }
}

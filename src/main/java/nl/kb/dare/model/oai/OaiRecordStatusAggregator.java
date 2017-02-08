package nl.kb.dare.model.oai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.util.Map;
/*
    @SqlQuery("select count(*) as count, oai_records.oai_status as oai_status, oai_records.process_status as process_status, repositories.oai_set as oai_set " +
            "from oai_records, repositories " +
            "where repositories.id = oai_records.repository_id " +
            "group by repository_id, process_status, oai_status")
 */
public class OaiRecordStatusAggregator {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String AGGREGATION_QUERY =
            "select count(*) as count, " +
                    "oai_records.process_status as process_status, " +
                    "repositories.oai_set as oai_set," +
                    "repositories.id as repository_id " +
            "from oai_records, repositories " +
            "where repositories.id = oai_records.repository_id " +
            "group by repository_id, process_status";

    private final DBI db;

    public OaiRecordStatusAggregator(DBI db) {
        this.db = db;
    }

    public String getStatus() throws JsonProcessingException {
        final Map<String, Map<String, Object>> resultMap = Maps.newHashMap();
        final Handle handle = db.open();

        for (Map<String, Object> row : handle.createQuery(AGGREGATION_QUERY)) {
            final String oaiSet = (String) row.get("oai_set");
            final String processStatus = (String) row.get("process_status");

            final Map<String, Object> statusMap = resultMap.getOrDefault(oaiSet, Maps.newHashMap());
            statusMap.put(processStatus, row.get("count"));
            statusMap.putIfAbsent("detail", String.format("/repositories/%d/records", (Integer) row.get("repository_id")));

            resultMap.put(oaiSet, statusMap);
        }

        handle.close();
        return OBJECT_MAPPER.writeValueAsString(resultMap);
    }
}

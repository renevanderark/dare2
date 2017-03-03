package nl.kb.dare.model.oai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Maps;
import nl.kb.dare.model.reporting.ProgressReport;
import nl.kb.dare.model.reporting.progress.DownloadProgressReport;
import nl.kb.dare.model.reporting.progress.GetRecordProgressReport;
import nl.kb.dare.model.statuscodes.ErrorStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static nl.kb.dare.model.reporting.progress.GetRecordProgressReport.ProgressStep.FINALIZE_MANIFEST;

public class OaiRecordStatusAggregator {

    private static final String RECORDS_QUERY =
            "select count(*) as count, " +
                    "oai_records.process_status_code as status_code, " +
                    "oai_records.repository_id as repository_id " +
            "from oai_records " +
            "group by repository_id, process_status_code";

    private static final String ERROR_QUERY =
            "select count(distinct(record_identifier)) as count," +
                    "oai_record_errors.status_code as status_code, " +
                    "oai_records.repository_id as repository_id " +
                    "from oai_record_errors, oai_records " +
            "where oai_records.identifier = oai_record_errors.record_identifier " +
            "group by repository_id, oai_record_errors.status_code";

    private final DBI db;
    private final OaiRecordQueryFactory oaiRecordQueryFactory;
    private final Map<String, Map<String, ProgressReport>> progressReportMap = Maps.newHashMap();

    public OaiRecordStatusAggregator(DBI db, OaiRecordQueryFactory oaiRecordQueryFactory) {
        this.db = db;
        this.oaiRecordQueryFactory = oaiRecordQueryFactory;
    }

    public Map<String, Map<String, Map<String, Object>>> getStatus() throws JsonProcessingException {
        final Map<String, Map<String, Map<String, Object>>> resultMap = Maps.newHashMap();

        final Map<String, Map<String, Object>> recordsMap = getAggregation(RECORDS_QUERY, true);
        final Map<String, Map<String, Object>> errorsMap = getAggregation(ERROR_QUERY, false);

        resultMap.put("recordStatus", recordsMap);
        resultMap.put("errorStatus", errorsMap);

        return resultMap;
    }


    public void digestProgressReport(ProgressReport progressReport) {
        final String recordIdentifier = progressReport.getRecordIdentifier();

        if (!progressReportMap.containsKey(recordIdentifier)) {
            progressReportMap.put(recordIdentifier, Maps.newHashMap());
        }

        final Map<String, ProgressReport> recordProgressReports = progressReportMap.get(recordIdentifier);

        final String mapKey = progressReport instanceof DownloadProgressReport
                ? "downloadProgress" : "getRecordProgress";

        recordProgressReports.put(mapKey, progressReport);

        if (progressReport instanceof GetRecordProgressReport && (
                ((GetRecordProgressReport) progressReport).getProgressStep() == FINALIZE_MANIFEST ||
                !((GetRecordProgressReport) progressReport).getSuccess()
            ) && progressReportMap.containsKey(recordIdentifier)) {

            progressReportMap.remove(recordIdentifier);
        }

        final Set<String> actualRecords = oaiRecordQueryFactory
                .getInstance(ProcessStatus.PROCESSING)
                .getResults(db)
                .stream()
                .map(OaiRecord::getIdentifier)
                .collect(toSet());

        for (String recordId : progressReportMap.keySet()) {
            if (!actualRecords.contains(recordId)) {
                progressReportMap.remove(recordId);
            }
        }

    }


    public Map<String, Map<String, ProgressReport>> getProgressReportMap() {

        return progressReportMap;
    }

    private Map<String, Map<String, Object>> getAggregation(String sql, boolean forRecords) {
        final Map<String, Map<String, Object>> result = Maps.newHashMap();
        final Handle handle = db.open();
        for (Map<String, Object> row : handle.createQuery(sql)) {
            final String repositoryId = String.format("%d", (Integer) row.get("repository_id"));
            final Integer statusCode = (Integer) row.get("status_code");

            final Map<String, Object> statusMap = result.getOrDefault(repositoryId, Maps.newHashMap());
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
            result.put(repositoryId, statusMap);
        }
        handle.close();
        return result;
    }
}

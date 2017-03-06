package nl.kb.dare.model.reporting.progress;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.reporting.ProgressReport;

public class GetRecordProgressReport implements ProgressReport {

    public boolean getSuccess() {
        return success;
    }

    public enum ProgressStep {
        DOWNLOAD_METADATA,
        GENERATE_MANIFEST,
        COLLECT_RESOURCES,
        DOWNLOAD_RESOURCES,
        FINALIZE_MANIFEST
    }

    @JsonProperty
    private final ProgressStep progressStep;
    @JsonProperty
    private final boolean success;
    @JsonProperty
    private final Integer repositoryId;

    private final String recordIdentifier;

    public GetRecordProgressReport(OaiRecord oaiRecord, ProgressStep progressStep, boolean success) {
        this.progressStep = progressStep;
        this.success = success;
        this.recordIdentifier = oaiRecord.getIdentifier();
        this.repositoryId = oaiRecord.getRepositoryId();
    }

    @Override
    public String getRecordIdentifier() {
        return recordIdentifier;
    }

    public ProgressStep getProgressStep() {
        return progressStep;
    }

    @Override
    public String toString() {
        return "GetRecordProgressReport{" +
                "progressStep=" + progressStep +
                ", success=" + success +
                ", recordIdentifier='" + recordIdentifier + '\'' +
                '}';
    }
}

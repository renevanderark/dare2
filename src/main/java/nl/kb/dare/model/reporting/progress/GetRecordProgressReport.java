package nl.kb.dare.model.reporting.progress;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.kb.dare.model.reporting.ProgressReport;

public class GetRecordProgressReport implements ProgressReport {
    public enum ProgressStep {
        GENERATE_MANIFEST, COLLECT_RESOURCES, DOWNLOAD_RESOURCES, FINALIZE_MANIFEST, DOWNLOAD_METADATA

    }

    @JsonProperty
    private final ProgressStep progressStep;
    @JsonProperty
    private final boolean success;

    public GetRecordProgressReport(ProgressStep progressStep, boolean success) {

        this.progressStep = progressStep;
        this.success = success;
    }
}

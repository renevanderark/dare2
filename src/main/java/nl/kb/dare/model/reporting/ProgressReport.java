package nl.kb.dare.model.reporting;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProgressReport {
    public enum ProgressStep {
        GENERATE_MANIFEST, COLLECT_RESOURCES, DOWNLOAD_RESOURCES, FINALIZE_MANIFEST, DOWNLOAD_METADATA

    }

    @JsonProperty
    private final ProgressStep progressStep;
    @JsonProperty
    private final boolean success;

    public ProgressReport(ProgressStep progressStep, boolean success) {

        this.progressStep = progressStep;
        this.success = success;
    }

}

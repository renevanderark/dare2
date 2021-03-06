package nl.kb.dare.model.reporting.progress;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.reporting.ProgressReport;

public class DownloadProgressReport implements ProgressReport {

    @JsonProperty
    private final String recordIdentifier;
    @JsonProperty
    private final Integer fileIndex;
    @JsonProperty
    private final Integer amountOfFiles;
    @JsonProperty
    private final String filename;
    @JsonProperty
    private final long currentByteCount;
    @JsonProperty
    private final Long expectedFileSize;

    public DownloadProgressReport(
            OaiRecord oaiRecord,
            Integer fileIndex,
            Integer amountOfFiles,
            String filename,
            long currentByteCount,
            Long expectedFileSize) {

        this.recordIdentifier = oaiRecord.getIdentifier();
        this.fileIndex = fileIndex;
        this.amountOfFiles = amountOfFiles;
        this.filename = filename;
        this.currentByteCount = currentByteCount;
        this.expectedFileSize = expectedFileSize;
    }

    @Override
    public String getRecordIdentifier() {
        return recordIdentifier;
    }

    @Override
    public String toString() {
        return "DownloadProgressReport{" +
                "recordIdentifier='" + recordIdentifier + '\'' +
                ", fileIndex=" + fileIndex +
                ", amountOfFiles=" + amountOfFiles +
                ", filename='" + filename + '\'' +
                ", currentByteCount=" + currentByteCount +
                ", expectedFileSize=" + expectedFileSize +
                '}';
    }
}

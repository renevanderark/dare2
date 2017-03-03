package nl.kb.dare.checksum;

import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.reporting.ProgressReport;

import java.util.function.Consumer;

public class ProgressReportingByteCountOutputStream extends ByteCountOutputStream {


    private final OaiRecord oaiRecord;
    private final Integer fileCount;
    private final Integer amountOfFiles;
    private final Consumer<ProgressReport> onProgress;

    public ProgressReportingByteCountOutputStream(
            OaiRecord oaiRecord, Integer fileIndex, Integer amountOfFiles, Consumer<ProgressReport> onProgress) {

        this.oaiRecord = oaiRecord;
        this.fileCount = fileIndex;
        this.amountOfFiles = amountOfFiles;
        this.onProgress = onProgress;
    }

    @Override
    public synchronized void write(int b) {
        super.write(b);
    }

    @Override
    public synchronized void write(byte b[], int off, int len) {
        super.write(b, off, len);
    }

}

package nl.kb.dare.checksum;

import com.google.common.collect.Lists;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.reporting.ProgressReport;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ProgressReportingByteCountOutputStream extends ByteCountOutputStream {


    private final OaiRecord oaiRecord;
    private final Integer fileCount;
    private final Integer amountOfFiles;
    private final Consumer<ProgressReport> onProgress;
    private Long expectedFileSize = -1L;

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

    public void readExpectedFileSize(Map<String, List<String>> headerFields) {
        final List<String> headerKeys = Lists.newArrayList("Content-Length", "Content-length", "content-length");
        for (String headerKey : headerKeys) {
            if (headerFields.containsKey(headerKey) && headerFields.get(headerKey).size() > 0) {
                try {
                    expectedFileSize = Long.parseLong(headerFields.get(headerKey).get(0));
                    break;
                } catch (NumberFormatException e) {
                    expectedFileSize = -1L;
                }
            }
        }

    }
}

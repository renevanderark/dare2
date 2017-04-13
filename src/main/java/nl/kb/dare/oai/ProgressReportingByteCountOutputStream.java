package nl.kb.dare.oai;

import com.google.common.collect.Lists;
import nl.kb.http.HeaderConsumer;
import nl.kb.stream.ByteCountOutputStream;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.reporting.ProgressReport;
import nl.kb.dare.model.reporting.progress.DownloadProgressReport;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ProgressReportingByteCountOutputStream extends ByteCountOutputStream implements HeaderConsumer {


    private final OaiRecord oaiRecord;
    private final Integer fileIndex;
    private final Integer amountOfFiles;
    private final String filename;
    private final Consumer<ProgressReport> onProgress;
    private Long expectedFileSize = -1L;

    private Instant lastWrite = null;

    ProgressReportingByteCountOutputStream(
            OaiRecord oaiRecord,
            Integer fileIndex,
            Integer amountOfFiles,
            String filename,
            Consumer<ProgressReport> onProgress) {

        this.oaiRecord = oaiRecord;
        this.fileIndex = fileIndex;
        this.amountOfFiles = amountOfFiles;
        this.filename = filename;
        this.onProgress = onProgress;
    }

    @Override
    public synchronized void write(int b) {
        super.write(b);
        reportProgress();
    }

    @Override
    public synchronized void write(byte b[], int off, int len) {
        super.write(b, off, len);
        reportProgress();
    }

    private void reportProgress() {
        if (lastWrite == null || expectedFileSize == getCurrentByteCount() ||
                Duration.between(lastWrite, Instant.now()).toMillis() > 200L) {
            onProgress.accept(new DownloadProgressReport(
                    oaiRecord, fileIndex, amountOfFiles, filename, getCurrentByteCount(), expectedFileSize
            ));
            lastWrite = Instant.now();
        }
    }


    @Override
    public void consumeHeaders(Map<String, List<String>> headerFields) {
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

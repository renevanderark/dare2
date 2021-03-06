package nl.kb.dare.oai;

import com.google.common.collect.Lists;
import nl.kb.http.HttpFetcher;
import nl.kb.http.HttpResponseHandler;
import nl.kb.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.mets.manifest.ObjectResource;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.reporting.ErrorReport;
import nl.kb.dare.model.reporting.ProgressReport;
import nl.kb.filestorage.FileStorageHandle;
import nl.kb.stream.ByteCountOutputStream;
import nl.kb.stream.ChecksumOutputStream;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

class GetRecordResourceOperations {
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;
    private final Consumer<ProgressReport> onProgress;

    GetRecordResourceOperations(HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory,
                                Consumer<ProgressReport> onProgress) {
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
        this.onProgress = onProgress;
    }

    List<ErrorReport> downloadResource(
            ObjectResource objectResource,
            FileStorageHandle fileStorageHandle,
            Integer fileIndex,
            Integer amountOfFiles,
            OaiRecord oaiRecord) throws IOException, NoSuchAlgorithmException {

        final String fileLocation = objectResource.getXlinkHref();
        final String filename = createFilename(fileLocation);

        final OutputStream objectOut = fileStorageHandle.getOutputStream("resources", filename);
        final ChecksumOutputStream checksumOut = new ChecksumOutputStream("SHA-512");
        final ProgressReportingByteCountOutputStream byteCountOut = new ProgressReportingByteCountOutputStream(
                oaiRecord,
                fileIndex,
                amountOfFiles,
                filename,
                onProgress
        );

        // First try to fetch the resource by encoding the url name one way (whitespace as '+')
        final String preparedUrlWithPluses = prepareUrl(fileLocation, false);
        final List<ErrorReport> firstAttemptErrors = attemptDownload(objectOut, checksumOut, byteCountOut,
                preparedUrlWithPluses);

        if (firstAttemptErrors.isEmpty()) {
            writeChecksumAndFilename(objectResource, checksumOut, byteCountOut, filename);
            return Lists.newArrayList();
        }

        // Then try to fetch the resource by encoding the url name another way (whitespace as '%20')
        final String preparedUrlWithPercents = prepareUrl(fileLocation, true);
        if (preparedUrlWithPercents.equals(preparedUrlWithPluses)) {
            return firstAttemptErrors;
        }

        final List<ErrorReport> secondAttemptErrors = attemptDownload(objectOut, checksumOut, byteCountOut,
                preparedUrlWithPercents);

        if (secondAttemptErrors.isEmpty()) {
            writeChecksumAndFilename(objectResource, checksumOut, byteCountOut, filename);
            return Lists.newArrayList();
        }

        return Stream
                .concat(firstAttemptErrors.stream(), secondAttemptErrors.stream())
                .collect(toList());
    }

    private void writeChecksumAndFilename(ObjectResource objectResource,
                                          ChecksumOutputStream checksumOut,
                                          ByteCountOutputStream byteCountOut,
                                          String filename) throws UnsupportedEncodingException {

        objectResource.setChecksum(checksumOut.getChecksumString());
        objectResource.setChecksumType("SHA-512");
        objectResource.setLocalFilename(filename);
        objectResource.setSize(byteCountOut.getCurrentByteCount());
    }

    private List<ErrorReport> attemptDownload(OutputStream objectOut, OutputStream checksumOut,
                                              ProgressReportingByteCountOutputStream byteCountOut,
                                              String preparedUrl) throws UnsupportedEncodingException, MalformedURLException {
        final HttpResponseHandler responseHandler = responseHandlerFactory
                .getStreamCopyingResponseHandler(objectOut, checksumOut, byteCountOut);

        final URL objectUrl = new URL(preparedUrl);

        httpFetcher.execute(objectUrl, responseHandler);

        return ErrorReport.fromExceptionList(responseHandler.getExceptions());
    }

    static String createFilename(String objectFile) throws MalformedURLException, UnsupportedEncodingException {
        final String decodedFilename = URLDecoder.decode(new URL(objectFile).getPath().replaceAll("/$", ""), "UTF8");

        return FilenameUtils.getName(decodedFilename);
    }

    private String prepareUrl(String rawUrl, boolean plusToPercent) throws UnsupportedEncodingException {
        final String name = FilenameUtils.getName(rawUrl);
        final String path = FilenameUtils.getPath(rawUrl);

        return path + encodeName(name, plusToPercent);
    }

    private String encodeName(String name, boolean plusToPercent) throws UnsupportedEncodingException {
        final String encodedName = name.equals(URLDecoder.decode(name, "UTF8"))
                ? URLEncoder.encode(name, "UTF8")
                : URLEncoder.encode(URLDecoder.decode(name, "UTF8"), "UTF8");

        return plusToPercent
                ? encodedName.replaceAll("\\+", "%20")
                : encodedName;
    }
}

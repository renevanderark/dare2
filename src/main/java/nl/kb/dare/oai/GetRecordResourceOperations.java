package nl.kb.dare.oai;

import com.google.common.collect.Lists;
import nl.kb.dare.files.FileStorageHandle;
import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.HttpResponseHandler;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.reporting.ErrorReport;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

class GetRecordResourceOperations {
    private static final Logger LOG = LoggerFactory.getLogger(GetRecordResourceOperations.class);
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;

    GetRecordResourceOperations(HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory) {
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
    }

    List<ErrorReport> downloadResource(ObjectResource objectResource, FileStorageHandle fileStorageHandle) throws IOException {
        final String fileLocation = objectResource.getXlinkHref();
        final String filename = createFilename(fileLocation);

        final OutputStream objectOut = fileStorageHandle.getOutputStream("resources", filename);
        final ByteArrayOutputStream checksumOut = new ByteArrayOutputStream();

        // First try to fetch the resource by encoding the url name one way (whitespace as '+')
        final List<ErrorReport> firstAttemptErrors = attemptDownload(fileLocation, objectOut, checksumOut, false);

        if (firstAttemptErrors.isEmpty()) {
            writeChecksumAndFilename(objectResource, checksumOut, filename);
            return Lists.newArrayList();
        }

        // Then try to fetch the resource by encoding the url name another way (whitespace as '%20')
        final List<ErrorReport> secondAttemptErrors = attemptDownload(fileLocation, objectOut, checksumOut, true);

        if (secondAttemptErrors.isEmpty()) {
            writeChecksumAndFilename(objectResource, checksumOut, filename);
            return Lists.newArrayList();
        }

        return Stream
                .concat(firstAttemptErrors.stream(), secondAttemptErrors.stream())
                .collect(toList());
    }

    private void writeChecksumAndFilename(ObjectResource objectResource, ByteArrayOutputStream checksumOut, String filename) throws UnsupportedEncodingException {
        objectResource.setChecksum(checksumOut.toString("UTF8"));
        objectResource.setChecksumType("MD5");
        objectResource.setLocalFilename(filename);
    }

    private List<ErrorReport> attemptDownload(String fileLocation, OutputStream objectOut, OutputStream checksumOut, boolean plusToPercent) throws UnsupportedEncodingException, MalformedURLException {
        final HttpResponseHandler responseHandler = responseHandlerFactory
                .getStreamCopyingResponseHandler(objectOut, checksumOut);
        final URL objectUrl = new URL(prepareUrl(fileLocation, plusToPercent));

        httpFetcher.execute(objectUrl, responseHandler);

        return responseHandler.getExceptions();
    }

    private String createFilename(String objectFile) throws MalformedURLException, UnsupportedEncodingException {
        final String decodedFilename = URLDecoder.decode(new URL(objectFile).getPath(), "UTF8");
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

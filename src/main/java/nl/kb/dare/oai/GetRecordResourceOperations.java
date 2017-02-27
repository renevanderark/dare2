package nl.kb.dare.oai;

import com.google.common.collect.Lists;
import nl.kb.dare.checksum.ChecksumOutputStream;
import nl.kb.dare.files.FileStorageHandle;
import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.HttpResponseHandler;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.reporting.ErrorReport;
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
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

class GetRecordResourceOperations {
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;

    GetRecordResourceOperations(HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory) {
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
    }

    List<ErrorReport> downloadResource(ObjectResource objectResource, FileStorageHandle fileStorageHandle)
            throws IOException, NoSuchAlgorithmException {
        final String fileLocation = objectResource.getXlinkHref();
        final String filename = createFilename(fileLocation);

        final OutputStream objectOut = fileStorageHandle.getOutputStream("resources", filename);
        final ChecksumOutputStream checksumOut = new ChecksumOutputStream("MD5");

        // First try to fetch the resource by encoding the url name one way (whitespace as '+')
        final String preparedUrlWithPluses = prepareUrl(fileLocation, false);
        final List<ErrorReport> firstAttemptErrors = attemptDownload(objectOut, checksumOut, preparedUrlWithPluses);

        if (firstAttemptErrors.isEmpty()) {
            writeChecksumAndFilename(objectResource, checksumOut, filename);
            return Lists.newArrayList();
        }

        // Then try to fetch the resource by encoding the url name another way (whitespace as '%20')
        final String preparedUrlWithPercents = prepareUrl(fileLocation, true);
        if (preparedUrlWithPercents.equals(preparedUrlWithPluses)) {
            return firstAttemptErrors;
        }

        final List<ErrorReport> secondAttemptErrors = attemptDownload(objectOut, checksumOut, preparedUrlWithPercents);

        if (secondAttemptErrors.isEmpty()) {
            writeChecksumAndFilename(objectResource, checksumOut, filename);
            return Lists.newArrayList();
        }

        return Stream
                .concat(firstAttemptErrors.stream(), secondAttemptErrors.stream())
                .collect(toList());
    }

    private void writeChecksumAndFilename(ObjectResource objectResource, ChecksumOutputStream checksumOut, String filename) throws UnsupportedEncodingException {
        objectResource.setChecksum(checksumOut.getChecksumString());
        objectResource.setChecksumType("MD5");
        objectResource.setLocalFilename(filename);
    }

    private List<ErrorReport> attemptDownload(OutputStream objectOut, OutputStream checksumOut, String preparedUrl) throws UnsupportedEncodingException, MalformedURLException {
        final HttpResponseHandler responseHandler = responseHandlerFactory
                .getStreamCopyingResponseHandler(objectOut, checksumOut);
        final URL objectUrl = new URL(preparedUrl);

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

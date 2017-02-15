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

class GetRecordResourceOperations {
    private static final Logger LOG = LoggerFactory.getLogger(GetRecordResourceOperations.class);
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;

    GetRecordResourceOperations(HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory) {
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
    }

    List<ErrorReport> downloadResource(ObjectResource objectResource, FileStorageHandle fileStorageHandle) throws IOException {
        final List<ErrorReport> errorReports = Lists.newArrayList();
        final String fileLocation = objectResource.getXlinkHref();
        final String filename = createFilename(fileLocation);
        final OutputStream objectOut = fileStorageHandle.getOutputStream("resources", filename);
        final ByteArrayOutputStream checksumOut = new ByteArrayOutputStream();

        final HttpResponseHandler responseHandler = responseHandlerFactory
                .getStreamCopyingResponseHandler(objectOut, checksumOut);

        final String preparedUrl = prepareUrl(fileLocation);
        final URL objectUrl = new URL(preparedUrl);
        httpFetcher.execute(objectUrl, responseHandler);
        if (!responseHandler.getExceptions().isEmpty()) {
            final HttpResponseHandler responseHandler2 = responseHandlerFactory
                    .getStreamCopyingResponseHandler(objectOut, checksumOut);
            final URL objectUrl2 = new URL(preparedUrl.replaceAll("\\+", "%20"));
            httpFetcher.execute(objectUrl2, responseHandler2);

            if (!responseHandler2.getExceptions().isEmpty()) {
                errorReports.addAll(responseHandler.getExceptions());
                errorReports.addAll(responseHandler2.getExceptions());
            }
        }

        objectResource.setChecksum(checksumOut.toString("UTF8"));
        objectResource.setChecksumType("MD5");
        LOG.info("Fetched resource: {}\nfilename: {}\nchecksum: {}",
                fileLocation, filename, objectResource.getChecksum());
        return errorReports;
    }


    private String createFilename(String objectFile) throws MalformedURLException, UnsupportedEncodingException {
        final String decodedFilename = URLDecoder.decode(new URL(objectFile).getPath(), "UTF8");
        return FilenameUtils.getName(decodedFilename);
    }

    private String prepareUrl(String rawUrl) throws UnsupportedEncodingException, MalformedURLException {
        final String name = FilenameUtils.getName(rawUrl);
        final String path = FilenameUtils.getPath(rawUrl);
        return name.equals(URLDecoder.decode(name, "UTF8")) ?
                path + URLEncoder.encode(name, "UTF8") :
                path + URLEncoder.encode(URLDecoder.decode(name, "UTF8"), "UTF8");
    }
}

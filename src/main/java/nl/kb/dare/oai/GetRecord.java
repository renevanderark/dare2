package nl.kb.dare.oai;

import com.google.common.collect.Lists;
import nl.kb.dare.files.FileStorage;
import nl.kb.dare.files.FileStorageHandle;
import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.HttpResponseHandler;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.reporting.ErrorReport;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.statuscodes.ErrorStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.dare.xslt.XsltTransformer;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

class GetRecord {
    private static final Logger LOG = LoggerFactory.getLogger(GetRecord.class);


    private final SAXParser saxParser;

    {
        try {
            saxParser = SAXParserFactory.newInstance().newSAXParser();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize sax parser", e);
        }
    }

    private final OaiRecord oaiRecord;
    private final Repository repositoryConfig;
    private final Consumer<ErrorReport> onError;
    private final FileStorage fileStorage;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;
    private final XsltTransformer xsltTransformer;
    private final boolean inSampleMode;

    GetRecord(OaiRecord oaiRecord, Repository repositoryConfig, FileStorage fileStorage,
              HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory,
              XsltTransformer xsltTransformer, Consumer<ErrorReport> onError,
              boolean inSampleMode) {

        this.oaiRecord = oaiRecord;
        this.repositoryConfig = repositoryConfig;
        this.fileStorage = fileStorage;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
        this.xsltTransformer = xsltTransformer;
        this.onError = onError;
        this.inSampleMode = inSampleMode;
    }

    ProcessStatus fetch() {
        final Optional<FileStorageHandle> fileStorageHandle = getFileStorageHandle(oaiRecord);
        if (!fileStorageHandle.isPresent()) {
            return ProcessStatus.FAILED;
        }

        if (!downloadMetadata(repositoryConfig, fileStorageHandle.get())) {
            return ProcessStatus.FAILED;
        }

        final boolean allResourcesDownloaded = downloadResources(fileStorageHandle.get());
        if (!allResourcesDownloaded) {
            return ProcessStatus.FAILED;
        }

        if (inSampleMode) {
            try {
                fileStorageHandle.get().deleteFiles();
            } catch (IOException e) {
                LOG.error("Failure trying to delete downloaded files while in sample mode", e);
            }
        }
        return ProcessStatus.PROCESSED;
    }

    private Optional<FileStorageHandle> getFileStorageHandle(OaiRecord oaiRecord) {
        try {
            return Optional.of(fileStorage.create(oaiRecord));
        } catch (IOException e) {
            onError.accept(new ErrorReport(
                new IOException("Failed to create storage location for record " + oaiRecord.getIdentifier(), e),
                ErrorStatus.IO_EXCEPTION)
            );
            return Optional.empty();
        }
    }

    private boolean downloadMetadata(Repository repository, FileStorageHandle fileStorageHandle) {
        try {
            final String urlStr = String.format("%s?verb=GetRecord&metadataPrefix=%s&identifier=%s",
                    repository.getUrl(), repository.getMetadataPrefix(), oaiRecord.getIdentifier());

            final OutputStream out = fileStorageHandle.getOutputStream("metadata.xml");
            LOG.info("fetching record: {}", urlStr);

            final HttpResponseHandler responseHandler = responseHandlerFactory
                    .getXsltTransformingHandler(new StreamResult(out), xsltTransformer);

            httpFetcher.execute(new URL(urlStr), responseHandler);

            responseHandler.getExceptions().forEach(onError);

            fileStorageHandle.syncFile(out);

            return responseHandler.getExceptions().isEmpty();
        } catch (IOException e) {
            onError.accept(new ErrorReport(e, ErrorStatus.IO_EXCEPTION));
            return false;
        }
    }


    private boolean downloadResources(FileStorageHandle fileStorageHandle) {
        try {

            final MetsXmlHandler metsXmlHandler = new MetsXmlHandler();

            synchronized (saxParser) {
                saxParser.parse(fileStorageHandle.getFile("metadata.xml"), metsXmlHandler);
            }

            final List<String> objectFiles = metsXmlHandler.getObjectFiles();

            if (objectFiles.isEmpty()) {
                onError.accept(new ErrorReport(
                    new IllegalArgumentException("No object files provided"),
                    ErrorStatus.NO_RESOURCES)
                );
                return false;
            }

            final List<ErrorReport> errorReports = Lists.newArrayList();
            for (String objectFile : objectFiles) {
                final String preparedUrl = prepareUrl(objectFile);

                final String filename = FilenameUtils.getName(new URL(objectFile).getPath());
                final String checksumFileName = filename + ".checksum";
                final OutputStream objectOut = fileStorageHandle.getOutputStream("resources", filename);
                final OutputStream checksumOut = fileStorageHandle.getOutputStream("resources", checksumFileName);


                final HttpResponseHandler responseHandler = responseHandlerFactory
                        .getStreamCopyingResponseHandler(objectOut, checksumOut);
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
                LOG.info("Fetched resource: {}", objectFile);
            }
            errorReports.forEach(onError);
            return errorReports.isEmpty();
        } catch (IOException e) {
            onError.accept(new ErrorReport(e, ErrorStatus.IO_EXCEPTION));
            return false;
        } catch (SAXException e) {
            onError.accept(new ErrorReport(e, ErrorStatus.XML_PARSING_ERROR));
            return false;
        }
    }

    private String prepareUrl(String rawUrl) throws UnsupportedEncodingException, MalformedURLException {
        final String name = FilenameUtils.getName(rawUrl);
        final String path = FilenameUtils.getPath(rawUrl);
        return name.equals(URLDecoder.decode(name, "UTF8")) ?
                path + URLEncoder.encode(name, "UTF8") :
                path + URLEncoder.encode(URLDecoder.decode(name, "UTF8"), "UTF8");
    }
}

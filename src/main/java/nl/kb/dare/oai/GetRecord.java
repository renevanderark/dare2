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
import java.net.URL;
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
    private FileStorage fileStorage;
    private HttpFetcher httpFetcher;
    private ResponseHandlerFactory responseHandlerFactory;
    private XsltTransformer xsltTransformer;

    GetRecord(OaiRecord oaiRecord, Repository repositoryConfig, FileStorage fileStorage,
              HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory,
              XsltTransformer xsltTransformer, Consumer<ErrorReport> onError) {

        this.oaiRecord = oaiRecord;
        this.repositoryConfig = repositoryConfig;
        this.fileStorage = fileStorage;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
        this.xsltTransformer = xsltTransformer;
        this.onError = onError;
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
                LOG.info("Fetching resource: {}", objectFile);
                final URL objectUrl = new URL(objectFile);

                final String filename = FilenameUtils.getName(objectUrl.getPath());
                final String checksumFileName = filename + ".checksum";
                final OutputStream objectOut = fileStorageHandle.getOutputStream("resources", filename);
                final OutputStream checksumOut = fileStorageHandle.getOutputStream("resources", checksumFileName);
                final HttpResponseHandler responseHandler = responseHandlerFactory
                        .getStreamCopyingResponseHandler(objectOut, checksumOut);
                httpFetcher.execute(objectUrl, responseHandler);
                errorReports.addAll(responseHandler.getExceptions());
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
}

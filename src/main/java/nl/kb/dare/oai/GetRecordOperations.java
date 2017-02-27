package nl.kb.dare.oai;

import com.google.common.collect.Lists;
import nl.kb.dare.checksum.ChecksumOutputStream;
import nl.kb.dare.files.FileStorage;
import nl.kb.dare.files.FileStorageHandle;
import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.HttpResponseHandler;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.reporting.ErrorReport;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.statuscodes.ErrorStatus;
import nl.kb.dare.xslt.XsltTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

class GetRecordOperations {
    private static final Logger LOG = LoggerFactory.getLogger(GetRecordOperations.class);

    private static final SAXParser saxParser;

    static {
        try {
            saxParser = SAXParserFactory.newInstance().newSAXParser();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize sax parser", e);
        }
    }

    private final FileStorage fileStorage;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;
    private final XsltTransformer xsltTransformer;
    private final Repository repository;
    private final Consumer<ErrorReport> onError;
    private final GetRecordResourceOperations resourceOperations;
    private final SipFinalizer sipFinalizer;

    GetRecordOperations(FileStorage fileStorage,
                        HttpFetcher httpFetcher,
                        ResponseHandlerFactory responseHandlerFactory,
                        XsltTransformer xsltTransformer,
                        Repository repository,
                        GetRecordResourceOperations resourceOperations,
                        SipFinalizer sipFinalizer, Consumer<ErrorReport> onError) {

        this.fileStorage = fileStorage;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
        this.xsltTransformer = xsltTransformer;
        this.repository = repository;
        this.resourceOperations = resourceOperations;
        this.sipFinalizer = sipFinalizer;
        this.onError = onError;
    }

    Optional<FileStorageHandle> getFileStorageHandle(OaiRecord oaiRecord) {
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

    Optional<ObjectResource> downloadMetadata(FileStorageHandle fileStorageHandle, OaiRecord oaiRecord) {
        try {
            final String urlStr = String.format("%s?verb=GetRecord&metadataPrefix=%s&identifier=%s",
                    repository.getUrl(), repository.getMetadataPrefix(), oaiRecord.getIdentifier());

            final OutputStream out = fileStorageHandle.getOutputStream("metadata.xml");
            final ChecksumOutputStream checksumOut = new ChecksumOutputStream("MD5");
            // final Writer outputStreamWriter = new OutputStreamWriter(out, "UTF8");
            LOG.info("fetching record: {}", urlStr);

            final HttpResponseHandler responseHandler = responseHandlerFactory
                    .getStreamCopyingResponseHandler(out, checksumOut);

            httpFetcher.execute(new URL(urlStr), responseHandler);

            responseHandler.getExceptions().forEach(onError);

            final ObjectResource objectResource = new ObjectResource();
            objectResource.setLocalFilename("metadata.xml");
            objectResource.setChecksum(checksumOut.getChecksumString());
            objectResource.setId("metadata");
            return responseHandler.getExceptions().isEmpty()
                    ? Optional.of(objectResource)
                    : Optional.empty();
        } catch (IOException | NoSuchAlgorithmException e) {
            onError.accept(new ErrorReport(e, ErrorStatus.IO_EXCEPTION));
            return Optional.empty();
        }
    }

    boolean generateManifest(FileStorageHandle handle) {
        try {
            final InputStream metadata = handle.getFile("metadata.xml");
            final OutputStream out = handle.getOutputStream("manifest.initial.xml");
            final Writer outputStreamWriter = new OutputStreamWriter(out, "UTF8");

            xsltTransformer.transform(metadata, new StreamResult(outputStreamWriter));

            return true;
        } catch (IOException e) {
            onError.accept(new ErrorReport(e, ErrorStatus.IO_EXCEPTION));
            return false;
        } catch (TransformerException e) {
            onError.accept(new ErrorReport(e, ErrorStatus.XML_PARSING_ERROR));
            return false;
        }
    }


    List<ObjectResource> collectResources(FileStorageHandle fileStorageHandle) {
        try {
            final MetsXmlHandler metsXmlHandler = new MetsXmlHandler();
            synchronized (saxParser) {
                saxParser.parse(fileStorageHandle.getFile("manifest.initial.xml"), metsXmlHandler);
            }
            final List<ObjectResource> objectResources = metsXmlHandler.getObjectResources();

            if (objectResources.isEmpty()) {
                onError.accept(new ErrorReport(
                        new IllegalArgumentException("No object files provided"),
                        ErrorStatus.NO_RESOURCES)
                );
            }

            return objectResources;
        } catch (SAXException e) {
            onError.accept(new ErrorReport(e, ErrorStatus.XML_PARSING_ERROR));
            return Lists.newArrayList();
        } catch (IOException e) {
            onError.accept(new ErrorReport(e, ErrorStatus.IO_EXCEPTION));
            return Lists.newArrayList();
        }
    }

    boolean downloadResources(FileStorageHandle fileStorageHandle, List<ObjectResource> objectResources) {
        try {
            final List<ErrorReport> errorReports = Lists.newArrayList();

            for (ObjectResource objectResource : objectResources) {

                errorReports.addAll(resourceOperations.downloadResource(objectResource, fileStorageHandle));

            }
            errorReports.forEach(onError);
            return errorReports.isEmpty();
        } catch (IOException | NoSuchAlgorithmException e) {
            onError.accept(new ErrorReport(e, ErrorStatus.IO_EXCEPTION));
            return false;
        }
    }


    boolean writeFilenamesAndChecksumsToMetadata(FileStorageHandle handle, List<ObjectResource> objectResources) {
        try {
            final InputStream in = handle.getFile("manifest.initial.xml");
            final OutputStream out = handle.getOutputStream("manifest.xml");
            final Reader metadata = new InputStreamReader(in,"UTF-8");
            final Writer sip = new OutputStreamWriter(out, "UTF-8");

            sipFinalizer.writeResourcesToSip(objectResources, metadata, sip);

            return true;
        } catch (IOException e) {
            onError.accept(new ErrorReport(e, ErrorStatus.IO_EXCEPTION));
            return false;
        } catch (SAXException | TransformerException e) {
            onError.accept(new ErrorReport(e, ErrorStatus.XML_PARSING_ERROR));
            return false;
        }
    }
}

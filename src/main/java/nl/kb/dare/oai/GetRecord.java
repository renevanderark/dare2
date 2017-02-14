package nl.kb.dare.oai;

import nl.kb.dare.files.FileStorage;
import nl.kb.dare.files.FileStorageHandle;
import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.reporting.ErrorReport;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.dare.xslt.XsltTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

class GetRecord {
    private static final Logger LOG = LoggerFactory.getLogger(GetRecord.class);


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
        final GetRecordOperations getRecordOperations = new GetRecordOperations(
                fileStorage, httpFetcher, responseHandlerFactory, xsltTransformer, onError);

        final Optional<FileStorageHandle> fileStorageHandle = getRecordOperations.getFileStorageHandle(oaiRecord);
        if (!fileStorageHandle.isPresent()) {
            return ProcessStatus.FAILED;
        }

        if (!getRecordOperations.downloadMetadata(repositoryConfig, fileStorageHandle.get(), oaiRecord)) {
            return ProcessStatus.FAILED;
        }

        final boolean allResourcesDownloaded = getRecordOperations.downloadResources(fileStorageHandle.get());
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



}

package nl.kb.dare.oai;

import nl.kb.dare.files.FileStorage;
import nl.kb.dare.files.FileStorageHandle;
import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.reporting.ErrorReport;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.dare.xslt.XsltTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class GetRecord {
    private static final Logger LOG = LoggerFactory.getLogger(GetRecord.class);


    private final OaiRecord oaiRecord;
    private final boolean inSampleMode;
    private final GetRecordOperations getRecordOperations;

    GetRecord(GetRecordOperations getRecordOperations, OaiRecord oaiRecord, boolean inSampleMode) {
        this.getRecordOperations = getRecordOperations;
        this.oaiRecord = oaiRecord;
        this.inSampleMode = inSampleMode;
    }

    public static ProcessStatus getAndRun(RepositoryDao repositoryDao, OaiRecord oaiRecord,
                                          HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory,
                                          FileStorage fileStorage, XsltTransformer xsltTransformer,
                                          Consumer<ErrorReport> onError, boolean inSampleMode) {

        final Repository repositoryConfig = repositoryDao.findById(oaiRecord.getRepositoryId());
        if (repositoryConfig == null) {
            LOG.error("SEVERE! OaiRecord missing repository configuration in database: {}", oaiRecord);
            // TODO error report
            return ProcessStatus.FAILED;
        }

        final GetRecordResourceOperations resourceOperations = new GetRecordResourceOperations(
                httpFetcher, responseHandlerFactory);

        final GetRecordOperations getRecordOperations = new GetRecordOperations(
                fileStorage, httpFetcher, responseHandlerFactory, xsltTransformer,
                repositoryConfig, resourceOperations, new ManifestFinalizer(),
                onError);

        return new GetRecord(getRecordOperations, oaiRecord, inSampleMode).fetch();
    }

    ProcessStatus fetch() {

        final Optional<FileStorageHandle> fileStorageHandle = getRecordOperations.getFileStorageHandle(oaiRecord);
        if (!fileStorageHandle.isPresent()) {
            return ProcessStatus.FAILED;
        }

        final FileStorageHandle handle = fileStorageHandle.get();
        final Optional<ObjectResource> metadataResource = getRecordOperations.downloadMetadata(handle, oaiRecord);
        if (!metadataResource.isPresent()) {
            return ProcessStatus.FAILED;
        }

        if (!getRecordOperations.generateManifest(handle)) {
            return ProcessStatus.FAILED;
        }

        final List<ObjectResource> objectResources = getRecordOperations.collectResources(handle);
        if (objectResources.isEmpty()) {
            return ProcessStatus.FAILED;
        }

        if (!getRecordOperations.downloadResources(handle, objectResources)) {
            return ProcessStatus.FAILED;
        }

        if (!getRecordOperations.writeFilenamesAndChecksumsToMetadata(handle, objectResources, metadataResource.get())) {
            return ProcessStatus.FAILED;
        }

        if (inSampleMode) {
            try {
                handle.deleteFiles();
            } catch (IOException e) {
                LOG.error("Failure trying to delete downloaded files while in sample mode", e);
            }
        }
        return ProcessStatus.PROCESSED;
    }
}

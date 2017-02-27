package nl.kb.dare.oai;

import nl.kb.dare.files.FileStorageHandle;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

class GetRecord {
    private static final Logger LOG = LoggerFactory.getLogger(GetRecord.class);


    private final OaiRecord oaiRecord;
    private final boolean inSampleMode;
    private final GetRecordOperations getRecordOperations;

    GetRecord(GetRecordOperations getRecordOperations, OaiRecord oaiRecord, boolean inSampleMode) {
        this.getRecordOperations = getRecordOperations;
        this.oaiRecord = oaiRecord;
        this.inSampleMode = inSampleMode;
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

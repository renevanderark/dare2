package nl.kb.dare.files;

import nl.kb.dare.model.oai.OaiRecord;

import java.io.IOException;

class LocalFileStorage implements FileStorage {
    private final String storageDir;

    LocalFileStorage(String storageDir) {
        this.storageDir = storageDir;
    }

    @Override
    public FileStorageHandle create(OaiRecord oaiRecord) throws IOException {
        return LocalFileStorageHandle.getInstance(oaiRecord, storageDir)
                .create();
    }
}

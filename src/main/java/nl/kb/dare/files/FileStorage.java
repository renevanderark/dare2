package nl.kb.dare.files;

import nl.kb.dare.model.oai.OaiRecord;

import java.io.IOException;

public interface FileStorage {

    FileStorageHandle create(OaiRecord oaiRecord) throws IOException;

    void purgeRepositoryFiles(Integer id) throws IOException;
}

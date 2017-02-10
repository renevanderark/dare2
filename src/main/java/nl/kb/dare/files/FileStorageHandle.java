package nl.kb.dare.files;

import java.io.IOException;

public interface FileStorageHandle {

    FileStorageHandle create() throws IOException;
    FileStorageHandle clear() throws IOException;
}

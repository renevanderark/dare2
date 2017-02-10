package nl.kb.dare.files;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;

public class FileStorageFactory {
    @JsonProperty
    private String storageType;

    @JsonProperty
    private String storageDir;

    public FileStorage getFileStorage() {
        switch (storageType) {
            case "local":
                final File fStorageDir = new File(storageDir);
                if (!fStorageDir.exists()) { throw new RuntimeException("Directory does not exist: " + storageDir); }
                if (!fStorageDir.isDirectory()) { throw new RuntimeException("File is not a directory: " + storageDir); }
                if (!fStorageDir.canWrite()) { throw new RuntimeException("No write permissions for directory: " + storageDir); }
                return new LocalFileStorage(storageDir);
            default:
                throw new RuntimeException("Unsupported file storage type");
        }
    }
}

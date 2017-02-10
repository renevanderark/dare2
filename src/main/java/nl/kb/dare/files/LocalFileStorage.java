package nl.kb.dare.files;

class LocalFileStorage implements FileStorage {
    private final String storageDir;

    LocalFileStorage(String storageDir) {
        this.storageDir = storageDir;
    }
}

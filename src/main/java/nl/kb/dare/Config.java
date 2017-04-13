package nl.kb.dare;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import nl.kb.filestorage.FileStorageFactory;

class Config extends Configuration {
    private DataSourceFactory database;
    private FileStorageFactory fileStorageFactory;

    @JsonProperty
    private String appTitle;
    @JsonProperty
    private String hostName;
    @JsonProperty
    private String wsProtocol;
    @JsonProperty
    private String databaseProvider;

    @JsonProperty
    private Boolean inSampleMode = false;

    @JsonProperty("database")
    DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @JsonProperty("database")
    void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.database = dataSourceFactory;
    }

    String getAppTitle() {
        return appTitle;
    }

    String getHostName() {
        return hostName;
    }

    String getWsProtocol() {
        return wsProtocol;
    }

    Boolean getInSampleMode() {
        return inSampleMode;
    }

    @JsonProperty("fileStorage")
    public FileStorageFactory getFileStorageFactory() {
        return fileStorageFactory;
    }

    @JsonProperty("fileStorage")
    public void setFileStorageFactory(FileStorageFactory fileStorageFactory) {
        this.fileStorageFactory = fileStorageFactory;
    }

    String getDatabaseProvider() {
        return databaseProvider;
    }
}

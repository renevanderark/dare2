package nl.kb.dare;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import nl.kb.dare.files.FileStorageFactory;

class Config extends Configuration {
    private DataSourceFactory database;
    private FileStorageFactory fileStorageFactory;

    @JsonProperty
    private String appTitle;
    @JsonProperty
    private String hostName;
    @JsonProperty
    private String wsProtocol;

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

    @JsonProperty("fileStorage")
    public FileStorageFactory getFileStorageFactory() {
        return fileStorageFactory;
    }

    @JsonProperty("fileStorage")
    public void setFileStorageFactory(FileStorageFactory fileStorageFactory) {
        this.fileStorageFactory = fileStorageFactory;
    }
}

package nl.kb.dare.model.repository;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Repository {

    private Integer id;
    private String url;
    private String metadataPrefix;
    private String set;
    private String dateStamp;

    public Repository() {

    }

    public Repository(String url, String metadataPrefix, String set, String dateStamp) {
        this.url = url;
        this.metadataPrefix = metadataPrefix;
        this.set = set;
        this.dateStamp = dateStamp;
    }

    public Repository(String url, String metadataPrefix, String set, String dateStamp, Integer id) {
        this(url, metadataPrefix, set, dateStamp);
        this.id = id;
    }

    @JsonProperty
    public String getUrl() {
        return url;
    }

    @JsonProperty
    public String getMetadataPrefix() {
        return metadataPrefix;
    }

    @JsonProperty
    public String getSet() {
        return set;
    }

    @JsonProperty
    public String getDateStamp() {
        return dateStamp;
    }

    @JsonProperty
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty
    public void setMetadataPrefix(String metadataPrefix) {
        this.metadataPrefix = metadataPrefix;
    }

    @JsonProperty
    public void setSet(String set) {
        this.set = set;
    }

    @JsonProperty
    public void setDateStamp(String dateStamp) {
        this.dateStamp = dateStamp;
    }

    @JsonProperty
    public Integer getId() {
        return id;
    }

    @JsonProperty
    public void setId(Integer id) {
        this.id = id;
    }
}

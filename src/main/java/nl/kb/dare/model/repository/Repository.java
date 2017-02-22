package nl.kb.dare.model.repository;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Repository {

    private Boolean enabled;
    private Integer id;
    private String url;
    private String metadataPrefix;
    private String set;
    private String dateStamp;
    private String name;

    public Repository() {

    }

    public Repository(String url, String name, String metadataPrefix, String set, String dateStamp, Boolean enabled) {
        this.url = url;
        this.name = name;
        this.metadataPrefix = metadataPrefix;
        this.set = set;
        this.dateStamp = dateStamp;
        this.enabled = enabled;
    }

    public Repository(String url, String name, String metadataPrefix, String set, String dateStamp, Boolean enabled, Integer id) {
        this(url, name, metadataPrefix, set, dateStamp, enabled);
        this.id = id;
    }

    @JsonProperty
    public String getUrl() {
        return url;
    }

    @JsonProperty
    public String getName() {
        return name;
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
    public void setName(String name) {
        this.name = name;
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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}

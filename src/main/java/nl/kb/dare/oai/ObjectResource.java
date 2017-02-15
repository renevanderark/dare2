package nl.kb.dare.oai;

class ObjectResource {

    private String id;
    private String xlinkHref;
    private String checksum;
    private String checksumType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getXlinkHref() {
        return xlinkHref;
    }

    public void setXlinkHref(String xlinkHref) {
        this.xlinkHref = xlinkHref;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public void setChecksumType(String checksumType) {
        this.checksumType = checksumType;
    }

    public String getChecksum() {
        return checksum;
    }
}

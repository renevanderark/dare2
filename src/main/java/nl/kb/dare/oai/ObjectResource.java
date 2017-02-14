package nl.kb.dare.oai;

class ObjectResource {

    private String id;
    private String xlinkHref;

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

    @Override
    public String toString() {
        return "ObjectResource{" +
                "id='" + id + '\'' +
                ", xlinkHref='" + xlinkHref + '\'' +
                '}';
    }
}

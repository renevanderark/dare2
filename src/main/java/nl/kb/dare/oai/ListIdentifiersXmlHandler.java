package nl.kb.dare.oai;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Optional;

class ListIdentifiersXmlHandler extends DefaultHandler {

    private static final String RESUMPTION_TOKEN = "resumptionToken";
    private boolean inResumptionToken = false;
    private boolean inDateStamp = false;
    private String resumptionToken = null;
    private String lastDateStamp;

    private ListIdentifiersXmlHandler() { }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (qName.equalsIgnoreCase(RESUMPTION_TOKEN)) {
            inResumptionToken = true;
        } else if(qName.equalsIgnoreCase("datestamp")) {
            inDateStamp = true;
        }
    }

    @Override
    public void characters(char ch[], int start, int length) {
        if (inResumptionToken) {
            this.resumptionToken = new String(ch, start, length);
        } else if (inDateStamp) {
            this.lastDateStamp = new String(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equalsIgnoreCase(RESUMPTION_TOKEN)) {
            inResumptionToken = false;
        } else if(qName.equalsIgnoreCase("datestamp")) {
            inDateStamp = false;
        }
    }

    Optional<String> getResumptionToken() {
        return resumptionToken == null || resumptionToken.trim().length() == 0
                ? Optional.empty()
                : Optional.of(resumptionToken);
    }

    Optional<String> getLastDateStamp() {
        return lastDateStamp == null || lastDateStamp.trim().length() == 0
                ? Optional.empty()
                : Optional.of(lastDateStamp);
    }

    static ListIdentifiersXmlHandler getNewInstance() {
        return new ListIdentifiersXmlHandler();
    }
}

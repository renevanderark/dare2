package nl.kb.dare.oai;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

class ListIdentifiersXmlHandler extends DefaultHandler {

    private static final String RESUMPTION_TOKEN = "resumptionToken";
    private boolean inResumptionToken;
    private String resumptionToken;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (qName.equalsIgnoreCase(RESUMPTION_TOKEN)) {
            inResumptionToken = true;
        }
    }

    @Override
    public void characters(char ch[], int start, int length) {
        if (inResumptionToken) {
            this.resumptionToken = new String(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equalsIgnoreCase(RESUMPTION_TOKEN)) {
            inResumptionToken = false;
        }
    }

    public String getResumptionToken() {
        return resumptionToken;
    }
}

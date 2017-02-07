package nl.kb.dare.oai;

import nl.kb.dare.model.oai.OaiRecord;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Optional;
import java.util.function.Consumer;

class ListIdentifiersXmlHandler extends DefaultHandler {

    private static final String RESUMPTION_TOKEN_ELEMENT = "resumptionToken";
    private static final String DATE_STAMP_ELEMENT = "datestamp";
    private static final String HEADER_ELEMENT = "header";
    public static final String IDENTIFIER_ELEMENT = "identifier";

    private final Integer repositoryId;
    private final Consumer<OaiRecord> onOaiRecord;

    private OaiRecord currentOaiRecord = null;

    private boolean inResumptionToken = false;
    private boolean inDateStamp = false;
    private boolean inIdentifier = false;

    private String resumptionToken = null;
    private String lastDateStamp = null;


    private ListIdentifiersXmlHandler(Integer repositoryId, Consumer<OaiRecord> onOaiRecord) {
        this.repositoryId = repositoryId;
        this.onOaiRecord = onOaiRecord;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        switch (qName) {
            case RESUMPTION_TOKEN_ELEMENT: startResumptionToken(); break;
            case DATE_STAMP_ELEMENT: startDateStamp(); break;
            case HEADER_ELEMENT: startOaiRecord(attributes); break;
            case IDENTIFIER_ELEMENT: startIdentifier(); break;
        }
    }

    @Override
    public void characters(char ch[], int start, int length) {
        if (inResumptionToken) {
            handleResumptionToken(ch, start, length);
        } else if (inDateStamp) {
            handleDateStamp(ch, start, length);
        } else if (inIdentifier) {
            handleIdentifier(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        switch (qName) {
            case RESUMPTION_TOKEN_ELEMENT: endResumptionToken(); break;
            case DATE_STAMP_ELEMENT: endDateStamp(); break;
            case HEADER_ELEMENT: endOaiRecord(); break;
            case IDENTIFIER_ELEMENT: endIdentifier(); break;

        }
    }

    private void handleIdentifier(char[] ch, int start, int length) {
        currentOaiRecord.setIdentifier(new String(ch, start, length));
    }


    private void handleDateStamp(char[] ch, int start, int length) {
        final String dateStamp = new String(ch, start, length);
        lastDateStamp = dateStamp;
        currentOaiRecord.setDateStamp(dateStamp);
    }

    private void handleResumptionToken(char[] ch, int start, int length) {
        resumptionToken = new String(ch, start, length);
    }

    private void startDateStamp() {
        inDateStamp = true;
    }

    private void startResumptionToken() {
        inResumptionToken = true;
    }

    private void startIdentifier() {
        inIdentifier = true;
    }

    private void endOaiRecord() {
        onOaiRecord.accept(currentOaiRecord);
    }

    private void endDateStamp() {
        inDateStamp = false;
    }

    private void endResumptionToken() {
        inResumptionToken = false;
    }

    private void endIdentifier() {
        inIdentifier = false;
    }

    private void startOaiRecord(Attributes attributes) {
        currentOaiRecord = new OaiRecord();
        currentOaiRecord.setRepositoryId(repositoryId);
        final String statusAttr = attributes.getValue("status");
        if (statusAttr != null && statusAttr.equalsIgnoreCase("deleted")) {
            currentOaiRecord.setStatus("deleted");
        } else {
            currentOaiRecord.setStatus("pending");
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

    static ListIdentifiersXmlHandler getNewInstance(Integer repositoryId, Consumer<OaiRecord> onOaiRecord) {

        return new ListIdentifiersXmlHandler(repositoryId, onOaiRecord);
    }
}

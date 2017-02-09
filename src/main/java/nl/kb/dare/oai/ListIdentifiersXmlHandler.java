package nl.kb.dare.oai;

import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.statuscodes.OaiStatus;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Optional;
import java.util.function.Consumer;

class ListIdentifiersXmlHandler extends DefaultHandler {

    private static final String RESUMPTION_TOKEN_ELEMENT = "resumptionToken";
    private static final String DATE_STAMP_ELEMENT = "datestamp";
    private static final String HEADER_ELEMENT = "header";
    private static final String IDENTIFIER_ELEMENT = "identifier";

    private final Integer repositoryId;
    private final Consumer<OaiRecord> onOaiRecord;

    private OaiRecord currentOaiRecord = new OaiRecord();

    private boolean inResumptionToken = false;
    private boolean inDateStamp = false;
    private boolean inIdentifier = false;

    private String resumptionToken = null;
    private String lastDateStamp = null;

    private StringBuilder resumptionTokenBuilder = new StringBuilder();
    private StringBuilder dateStampBuilder = new StringBuilder();
    private StringBuilder identifierBuilder = new StringBuilder();

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
        identifierBuilder.append(getStrippedText(ch, start, length));
    }


    private void handleDateStamp(char[] ch, int start, int length) {
        dateStampBuilder.append(getStrippedText(ch, start, length));
    }

    private void handleResumptionToken(char[] ch, int start, int length) {
        resumptionTokenBuilder.append(getStrippedText(ch, start, length));
    }


    private String getStrippedText(char[] ch, int start, int length) {
        return new String(ch, start, length)
                .replaceAll("\0", "")
                .replaceAll("\\r\\n", "")
                .replaceAll("\\n", "");
    }

    private void startDateStamp() {
        inDateStamp = true;
        dateStampBuilder = new StringBuilder();
    }

    private void startResumptionToken() {
        inResumptionToken = true;
        resumptionTokenBuilder = new StringBuilder();
    }

    private void startIdentifier() {
        inIdentifier = true;
        identifierBuilder = new StringBuilder();
    }

    private void endOaiRecord() {
        onOaiRecord.accept(currentOaiRecord);
    }

    private void endDateStamp() {

        inDateStamp = false;
        lastDateStamp = dateStampBuilder.toString();
        currentOaiRecord.setDateStamp(dateStampBuilder.toString());
    }

    private void endResumptionToken() {
        inResumptionToken = false;
        resumptionToken = resumptionTokenBuilder.toString();
    }

    private void endIdentifier() {
        inIdentifier = false;
        currentOaiRecord.setIdentifier(identifierBuilder.toString());
    }

    private void startOaiRecord(Attributes attributes) {
        currentOaiRecord = new OaiRecord();
        currentOaiRecord.setRepositoryId(repositoryId);
        final String statusAttr = attributes.getValue("status");
        if (statusAttr != null && statusAttr.equalsIgnoreCase("deleted")) {
            currentOaiRecord.setOaiStatus(OaiStatus.DELETED);
            currentOaiRecord.setProcessStatus("skip");
        } else {
            currentOaiRecord.setOaiStatus(OaiStatus.AVAILABLE);
            currentOaiRecord.setProcessStatus("pending");
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

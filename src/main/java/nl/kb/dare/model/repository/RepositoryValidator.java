package nl.kb.dare.model.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;

public class RepositoryValidator {
    private final HttpClient oaiHarvestClient;
    private final SAXParser saxParser;
    {
        try {
            saxParser = SAXParserFactory.newInstance().newSAXParser();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public RepositoryValidator(HttpClient oaiHarvestClient) {
        this.oaiHarvestClient = oaiHarvestClient;
    }

    public ValidationResult validate(Repository repositoryConfig) throws IOException, SAXException {
        final HttpGet listSetsGet = new HttpGet(String.format("%s?verb=ListSets", repositoryConfig.getUrl()));
        final HttpGet listMdGet = new HttpGet(String.format("%s?verb=ListMetadataFormats", repositoryConfig.getUrl()));
        final HttpResponse listSetsResponse = oaiHarvestClient.execute(listSetsGet);
        final HttpResponse listMdResponse = oaiHarvestClient.execute(listMdGet);

        final ValidationResult validationResult = new ValidationResult();
        final ListSetsHandler listSetsHandler = new ListSetsHandler(repositoryConfig.getSet(), validationResult);
        final ListMetadataFormatsHandler listMetadataFormatsHandler =
                new ListMetadataFormatsHandler(repositoryConfig.getMetadataPrefix(), validationResult);

        saxParser.parse(listSetsResponse.getEntity().getContent(), listSetsHandler);
        saxParser.parse(listMdResponse.getEntity().getContent(), listMetadataFormatsHandler);

        return validationResult;
    }

    private static class ListSetsHandler extends DefaultHandler {
        private final String SET_SPEC = "setSpec";
        private final String expectedSet;
        private ValidationResult validationResult;

        private boolean inSetSpec = false;

        ListSetsHandler(String expectedSet, ValidationResult validationResult) {
            this.expectedSet = expectedSet;
            this.validationResult = validationResult;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if (qName.equalsIgnoreCase(SET_SPEC)) {
                inSetSpec = true;
            }
        }

        @Override
        public void characters(char ch[], int start, int length) {
            if (inSetSpec && new String(ch, start, length).trim().equalsIgnoreCase(expectedSet)) {
                validationResult.setExists = true;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (qName.equalsIgnoreCase(SET_SPEC)) {
                inSetSpec = false;
            }
        }
    }

    private static class ListMetadataFormatsHandler extends DefaultHandler {

        private static final String METADATA_PREFIX = "metadataPrefix";
        private final String expectedMetadataPrefix;
        private final ValidationResult validationResult;
        private boolean inMetadataPrefix = false;

        ListMetadataFormatsHandler(String expectedMetadataPrefix, ValidationResult validationResult) {
            this.expectedMetadataPrefix = expectedMetadataPrefix;
            this.validationResult = validationResult;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if (qName.equalsIgnoreCase(METADATA_PREFIX)) {
                inMetadataPrefix = true;
            }
        }

        @Override
        public void characters(char ch[], int start, int length) {
            if (inMetadataPrefix && new String(ch, start, length).trim().equalsIgnoreCase(expectedMetadataPrefix)) {
                validationResult.metadataFormatSupported = true;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (qName.equalsIgnoreCase(METADATA_PREFIX)) {
                inMetadataPrefix = false;
            }
        }
    }

    public class ValidationResult {
        @JsonProperty
        Boolean setExists = false;
        @JsonProperty
        Boolean metadataFormatSupported = false;
    }
}

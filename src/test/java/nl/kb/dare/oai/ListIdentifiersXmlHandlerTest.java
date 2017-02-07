package nl.kb.dare.oai;

import com.google.common.collect.Lists;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.repository.RepositoryValidatorTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ListIdentifiersXmlHandlerTest {
    private InputStream xmlWithNewlines;
    private SAXParser saxParser;

    @Before
    public void setup() throws ParserConfigurationException, SAXException {

        saxParser = SAXParserFactory.newInstance().newSAXParser();
        xmlWithNewlines = RepositoryValidatorTest.class.getResourceAsStream("/oai/response-with-newlines.xml");
    }


    @After
    public void tearDown() {
        try {
            xmlWithNewlines.close();
        } catch (IOException ignored) {

        }
    }

    @Test
    public void itShouldStripNewlinesFromCDATA() throws IOException, SAXException {
        final List<OaiRecord> oaiRecords = Lists.newArrayList();
        final Consumer<OaiRecord> onOaiRecord = oaiRecords::add;
        final ListIdentifiersXmlHandler instance = ListIdentifiersXmlHandler.getNewInstance(123, onOaiRecord);

        saxParser.parse(xmlWithNewlines, instance);

        final String resumptionToken = instance.getResumptionToken().get();
        final String dateStamp = instance.getLastDateStamp().get();
        assertThat(oaiRecords.stream().map(OaiRecord::getIdentifier)
                .anyMatch((identifier) -> identifier.contains("\n") || identifier.contains("\r\n")), is(false));

        assertThat(resumptionToken.contains("\n") || resumptionToken.contains("\r\n"), is(false));

        assertThat(dateStamp.contains("\n") || dateStamp.contains("\r\n"), is(false));
    }
}
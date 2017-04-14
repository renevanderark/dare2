package nl.kb.oaipmh;

public class ListIdentifiersXmlHandlerTest {
/*    private InputStream xmlWithNewlines;
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

    @Test
    public void inShouldConcatenateChunkedCDATA() {
        final List<OaiRecord> oaiRecords = Lists.newArrayList();
        final Consumer<OaiRecord> onOaiRecord = oaiRecords::add;
        final ListIdentifiersXmlHandler instance = ListIdentifiersXmlHandler.getNewInstance(123, onOaiRecord);

        instance.startElement("", "", "header", mock(Attributes.class));

        instance.startElement("", "", "identifier", mock(Attributes.class));
        instance.characters(">doNot".toCharArray(), 1, 5);
        instance.characters("SplitMe<".toCharArray(), 0, 7);
        instance.endElement("","","identifier");

        instance.startElement("", "", "datestamp", mock(Attributes.class));
        instance.characters(">doNot".toCharArray(), 1, 5);
        instance.characters("SplitMe<".toCharArray(), 0, 7);
        instance.endElement("","","datestamp");

        instance.endElement("", "", "header");

        instance.startElement("", "", "resumptionToken", mock(Attributes.class));
        instance.characters(">doNot".toCharArray(), 1, 5);
        instance.characters("SplitMe<".toCharArray(), 0, 7);
        instance.endElement("","","resumptionToken");

        assertThat(oaiRecords.get(0), allOf(
            hasProperty("identifier", is("doNotSplitMe")),
            hasProperty("dateStamp", is("doNotSplitMe"))
        ));

        assertThat(instance.getLastDateStamp().get(), is("doNotSplitMe"));
        assertThat(instance.getResumptionToken().get(), is("doNotSplitMe"));
    }

    @Test
    public void itShouldNotThrowNullPointerExceptionsForInvalidOaiResponseXML() {
        final List<OaiRecord> oaiRecords = Lists.newArrayList();
        final Consumer<OaiRecord> onOaiRecord = oaiRecords::add;
        final ListIdentifiersXmlHandler instance = ListIdentifiersXmlHandler.getNewInstance(123, onOaiRecord);

        instance.startElement("", "", "identifier", mock(Attributes.class));
        instance.characters(">doNot".toCharArray(), 1, 5);
        instance.characters("SplitMe<".toCharArray(), 0, 7);
        instance.endElement("","","identifier");

    }*/
}
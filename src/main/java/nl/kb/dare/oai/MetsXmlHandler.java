package nl.kb.dare.oai;

import com.google.common.collect.Lists;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.List;

public class MetsXmlHandler extends DefaultHandler {

    private final List<String> objectFiles = Lists.newArrayList();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equals("mets:FLocat")) {
           objectFiles.add(attributes.getValue("xlink:href"));
        }
    }

    public List<String> getObjectFiles() {
        return objectFiles;
    }
}

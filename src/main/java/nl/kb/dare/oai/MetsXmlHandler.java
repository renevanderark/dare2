package nl.kb.dare.oai;

import com.google.common.collect.Lists;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.List;

public class MetsXmlHandler extends DefaultHandler {


    private ObjectResource currentResource = new ObjectResource();
    private final List<ObjectResource> objectResources = Lists.newArrayList();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equals("mets:file")) {
            currentResource = new ObjectResource();
            currentResource.setId(attributes.getValue("ID"));
        } else if (qName.equals("mets:FLocat")) {
            currentResource.setXlinkHref(attributes.getValue("xlink:href"));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("mets:file")) {
            objectResources.add(currentResource);
        }
    }

    public List<ObjectResource> getObjectResources() {
        return objectResources;
    }
}

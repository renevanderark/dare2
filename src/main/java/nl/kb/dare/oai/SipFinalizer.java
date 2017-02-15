package nl.kb.dare.oai;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;

public class SipFinalizer {

    private static final DocumentBuilder docBuilder;
    private static final TransformerFactory transformerFactory;

    static {
        try {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            docBuilder = documentBuilderFactory.newDocumentBuilder();
            transformerFactory = TransformerFactory.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize sax parser", e);
        }
    }

    static final String METS_NS = "http://www.loc.gov/METS/";
    static final String XLINK_NS = "http://www.w3.org/1999/xlink";

    public void writeResourcesToSip(List<ObjectResource> objectResources, Reader metadata, Writer sip)
            throws IOException, SAXException, TransformerException {

        synchronized (docBuilder) {
            final Document document = docBuilder.parse(new InputSource(metadata));
            final NodeList fileNodes = document.getElementsByTagNameNS(METS_NS, "file");
            final Transformer transformer = transformerFactory.newTransformer();

            for (int i = 0; i < fileNodes.getLength(); i++) {
                final Node fileNode = fileNodes.item(i);
                final NamedNodeMap fileAttributes = fileNode.getAttributes();
                final Node checksum = document.createAttribute("CHECKSUM");
                final Node checksumType = document.createAttribute("CHECKSUMTYPE");
                final String fileId = fileAttributes.getNamedItem("ID").getNodeValue();
                final Node fLocatNode = getFLocatNode(fileNode);


                final Optional<ObjectResource> currentResource = objectResources
                        .stream().filter(obj -> obj.getId() != null && obj.getId().equals(fileId))
                        .findAny();

                if (!currentResource.isPresent()) {
                    throw new IOException("Expected file resource is not present for metadata.xml: " + fileId);
                }

                checksum.setNodeValue(currentResource.get().getChecksum());
                checksumType.setNodeValue(currentResource.get().getChecksumType());
                fileAttributes.setNamedItem(checksum);
                fileAttributes.setNamedItem(checksumType);
                fLocatNode.getAttributes().getNamedItemNS(XLINK_NS, "href").setNodeValue(
                        "file://./resources/" +
                                URLEncoder.encode(currentResource.get().getLocalFilename(), "UTF8")
                                        .replaceAll("\\+", "%20")
                );
            }
            transformer.transform(new DOMSource(document), new StreamResult(sip));
        }
    }

    private Node getFLocatNode(Node fileNode) {
        final NodeList childNodes = fileNode.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node item = childNodes.item(i);
            if (item.getLocalName() != null && item.getLocalName().equalsIgnoreCase("flocat")) {
                return item;
            }
        }
        return null;
    }
}

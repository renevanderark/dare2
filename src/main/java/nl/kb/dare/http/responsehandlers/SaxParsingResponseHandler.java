package nl.kb.dare.http.responsehandlers;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.ws.rs.core.Response;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

class SaxParsingResponseHandler extends ErrorReportingResponseHandler  {
    private final SAXParser saxParser;

    {
        try {
            saxParser = SAXParserFactory.newInstance().newSAXParser();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize sax parser", e);
        }
    }

    private final DefaultHandler xmlHandler;

    SaxParsingResponseHandler(DefaultHandler xmlHandler) {
        this.xmlHandler = xmlHandler;
    }

    @Override
    public void onResponseData(Response.Status status, InputStream responseData) {
        try {
            final Reader reader = new InputStreamReader(responseData,"UTF-8");
            final InputSource inputSource = new InputSource(reader);
            saxParser.parse(inputSource, xmlHandler);
        } catch (SAXException e) {
            saxExceptions.add(e);
        } catch (IOException e) {
            ioExceptions.add(e);
        }
    }
}

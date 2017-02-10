package nl.kb.dare.xslt;

import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public interface XsltTransformer {

    void transform(InputStream in, Result out) throws TransformerException, UnsupportedEncodingException;
}

package nl.kb.dare.checksum;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ChecksumOutputStreamTest {

    @Test
    public void itShouldWriteAChecksum() throws IOException, NoSuchAlgorithmException {
        final ChecksumOutputStream instance = new ChecksumOutputStream("MD5");
        final InputStream in = ChecksumOutputStreamTest.class.getResourceAsStream("/http/text.txt");

        IOUtils.copy(in, instance);

        assertThat(instance.getChecksumString(), is("ae2b1fca515949e5d54fb22b8ed95575"));
    }
}
package br.gov.go.saude.dbc;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static br.gov.go.saude.TestUtils.assertStreamsEquals;
import static org.junit.Assert.*;

public class DBCInputStreamTest {

    @Test
    public void testUncompressDBF() throws IOException {
        InputStream decoded = new DBCInputStream(getClass().getResourceAsStream("/RDGO1301.dbc"));
        InputStream plain = getClass().getResourceAsStream("/RDGO1301.dbf");

        assertStreamsEquals(plain, decoded);
    }

    @Test
    public void testInvalidHeader() {
        InputStream input = new ByteArrayInputStream("hello world".getBytes());

        try {
            new DBCProcessor(input).readHeader();
        } catch (IOException ex) {
            assertTrue("Expected DBCFormatException", ex instanceof DBCFormatException);
        }
    }

    @Test
    public void testAvailableBytes() throws IOException {
        InputStream decoded = new DBCInputStream(getClass().getResourceAsStream("/RDGO1301.dbc"));

        assertTrue(decoded.available() > 0);

        while (decoded.available() > 0)
            decoded.read();
        assertEquals(0, decoded.available());

        assertNotEquals(-1, decoded.read());
        assertTrue(decoded.available() > 0);
    }

    @Test
    public void testClose() throws IOException {
        InputStream decoded = new DBCInputStream(getClass().getResourceAsStream("/RDGO1301.dbc"));

        for (int i = 0; i < 1024 * 8; i++)
            assertNotEquals(-1, decoded.read());

        decoded.close();
        assertEquals(-1, decoded.read());
    }
}

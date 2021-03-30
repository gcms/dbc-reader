package com.github.gcms.blast;


import org.junit.Test;

import java.io.InputStream;

import static com.github.gcms.TestUtils.getContent;
import static org.junit.Assert.assertEquals;

public class BlastInputStreamTest {


    @Test
    public void testSimple() {
        InputStream input = new BlastInputStream(getClass().getResourceAsStream("/test.pk"));

        assertEquals("AIAIAIAIAIAIA", getContent(input));
    }

    @Test
    public void testLipsum() {
        InputStream decoded = new BlastInputStream(getClass().getResourceAsStream("/lipsum.pk"));
        InputStream plain = getClass().getResourceAsStream("/lipsum.txt");

        assertEquals(getContent(plain), getContent(decoded));
    }
}

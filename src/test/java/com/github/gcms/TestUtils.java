package com.github.gcms;

import org.junit.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class TestUtils {
    public static void assertStreamsEquals(InputStream a, InputStream b) throws IOException {
        long pos = 0;
        while (true) {
            int av = a.read();
            int bv = b.read();

            Assert.assertEquals("Difference at " + pos++, av, bv);
            if (av == -1)
                break;
        }
    }


    public static String getContent(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        return reader.lines().collect(Collectors.joining("\n"));
    }
}

package com.github.gcms.dbc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads data from DBF header in a DBC file.
 */
class DBCHeaderReader extends InputStream {
    private final InputStream source;

    DBCHeaderReader(InputStream source) {
        this.source = source;
    }

    private InputStream input;

    private InputStream getInput() throws IOException {
        if (input == null)
            input = new ByteArrayInputStream(DBCProcessor.read(source));

        return input;
    }

    @Override
    public int read() throws IOException {
        return getInput().read();
    }

    @Override
    public int read(byte[] buf, int offset, int len) throws IOException {
        return getInput().read(buf, offset, len);
    }

    @Override
    public int available() throws IOException {
        return getInput().available();
    }

}

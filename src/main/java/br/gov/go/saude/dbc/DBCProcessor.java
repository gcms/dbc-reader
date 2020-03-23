package br.gov.go.saude.dbc;

import java.io.IOException;
import java.io.InputStream;

public class DBCProcessor {
    private final InputStream input;

    public DBCProcessor(InputStream input) {
        this.input = input;
    }

    private static int readLittleEndian(int a, int b) {
        return (a & 0xff) + (b << 8);
    }

    public byte[] readHeader() throws IOException {
        // first 8 bytes in the header
        byte[] headerStart = new byte[10];
        readAndCheck(headerStart);

        // header size at bytes 8-9
        int headerLength = readLittleEndian(headerStart[8], headerStart[9]);


        byte[] header = new byte[headerLength];
        System.arraycopy(headerStart, 0, header, 0, 10);

        // remaining bytes in the header
        readAndCheck(header, 10, headerLength - 10);

        // jump to position (headerLength + 4)
        check(input.skip(4), 4);

        return header;
    }

    private void readAndCheck(byte[] data) throws IOException {
        readAndCheck(data, 0, data.length);
    }

    private void readAndCheck(byte[] data, int offset, int len) throws IOException {
        check(input.read(data, offset, len), len);
    }

    private static void check(long actual, long expected) throws DBCFormatException {
        if (actual != expected)
            throw new DBCFormatException("Wrong DBC file header format, premature end of file");
    }


    public static byte[] read(InputStream is) throws IOException {
        return new DBCProcessor(is).readHeader();
    }

}

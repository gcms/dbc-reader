package br.gov.go.saude.dbc;

import net.sf.mpxj.primavera.common.Blast;

import java.io.*;

public class DBCProcessor {
    private final InputStream input;
    private final OutputStream output;
    private final Blast blast;

    public DBCProcessor(InputStream input, OutputStream output) {
        this.input = input;
        this.output = output;
        this.blast = new Blast();
    }

    private static int parseShortLittleEndian(int a, int b) {
        return (b << 8) + (a & 0xff);
    }

    public void copyHeader() throws IOException {
        // read first 8 bytes of the header
        byte[] headerStart = new byte[10];
        readAndCheck(headerStart);

        // read header size at bytes 8-9
        int headerLength = parseShortLittleEndian(headerStart[8], headerStart[9]);

        byte[] headerRemaining = new byte[headerLength - headerStart.length];
        readAndCheck(headerRemaining);

        // write header
        output.write(headerStart);
        output.write(headerRemaining);

        // jump to position (headerLength + 4)
        check(input.skip(4), 4);
    }

    private void readAndCheck(byte[] data) throws IOException {
        check(input.read(data), data.length);
    }

    private static void check(long actual, long expected) throws DBCFormatException {
        if (actual != expected)
            throw new DBCFormatException("Wrong DBC file header format");
    }

    public void blast() throws IOException {
        int code = blast.blast(input, output);
        if (code != 0)
            throw new DBCFormatException(code);
    }

    public void decompress() throws IOException {
        copyHeader();
        blast();
    }

    public static void blast(InputStream is, OutputStream os) throws IOException {
        new DBCProcessor(is, os).blast();
    }

    public static void decompress(InputStream is, OutputStream os) throws IOException {
        new DBCProcessor(is, os).decompress();
    }

}

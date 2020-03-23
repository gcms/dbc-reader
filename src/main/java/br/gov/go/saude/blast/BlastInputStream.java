package br.gov.go.saude.blast;

import java.io.IOException;
import java.io.InputStream;

/**
 * Implements a stream for uncompressing data in the PKWare Data Compression
 * Library (DCL) compressed format
 */
public class BlastInputStream extends InputStream {
    /* maximum window size */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 8;

    private final BlastDecoder decoder;
    private final BlastBuffer buffer;
    private final InputStream in;

    private boolean hasMoreData = true;

    /**
     * Creates a new input stream with a default buffer size.
     *
     * @param in the input stream
     */
    public BlastInputStream(InputStream in) {
        this(in, DEFAULT_BUFFER_SIZE, true);
    }

    /**
     * Creates a new input stream with an specified buffer size.
     *
     * @param in         the input stream
     * @param bufferSize the output buffer size
     * @param first      <code>true</code> to check distances (for first 4K)
     */
    public BlastInputStream(InputStream in, int bufferSize, boolean first) {
        this.in = in;
        this.decoder = new BlastDecoder(new BlastInput(in), first);
        this.buffer = new BlastBuffer(bufferSize);
    }


    /**
     * Reads a byte of uncompressed data.
     *
     * @return the byte read, or -1 if end of compressed input is reached
     * @throws IOException          if an I/O error has occurred
     * @throws BlastFormatException if the compressed input is malformed.
     */
    @Override
    public int read() throws IOException, BlastFormatException {
        return checkBuffer() ? buffer.read() & 0xff : -1;
    }

    /**
     * Reads uncompressed data into an array of bytes.
     *
     * @param buf    the buffer into which the data is read
     * @param offset the start offset in the destination array buf
     * @param len    the maximum number of bytes read
     * @return the actual number of bytes read, or -1 if the end of the compressed input is reached
     * @throws IOException          if an I/O error has occurred
     * @throws BlastFormatException if the compressed input is malformed.
     */
    @Override
    public int read(byte[] buf, int offset, int len) throws IOException, BlastFormatException {
        return checkBuffer() ? buffer.read(buf, offset, len) : -1;
    }

    private boolean checkBuffer() throws IOException {
        if (buffer.isEmpty()) {
            buffer.clear();

            while (hasMoreData && !buffer.isFull()) {
                hasMoreData = decoder.read(buffer);
            }
        }

        return !buffer.isEmpty();
    }

    /**
     * Returns the number of bytes available in the current uncompressed
     * byte buffer.
     *
     * @return the current buffer size
     */
    @Override
    public int available() {
        return buffer.available();
    }

    /**
     * Closes the inner input stream passed as parameter to the constructor.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void close() throws IOException {
        in.close();
    }
}

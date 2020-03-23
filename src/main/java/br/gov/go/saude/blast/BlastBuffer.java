package br.gov.go.saude.blast;


/**
 * Buffer for storing uncompressed data.
 */
public class BlastBuffer {

    private final byte[] buffer;
    private int pos, count;

    /**
     * Creates a new <code>BlastBuffer</code> and allocates the space.
     *
     * @param bufferSize the buffer size
     */
    BlastBuffer(int bufferSize) {
        this.buffer = new byte[bufferSize];
    }

    /**
     * Check if the buffer is empty, i.e. no data has been written or all
     * written data has been read.
     *
     * @return true if the buffer is empty; false otherwise
     */
    public boolean isEmpty() {
        return pos == count;
    }

    /**
     * Check if the buffer is full, i.e. data has been written to it's end.
     *
     * @return true if the buffer is full; false otherwise.
     */
    public boolean isFull() {
        return count == buffer.length;
    }


    /**
     * Returns the current number of bytes stored in the buffer.
     *
     * @return the buffer current size.
     */
    public int size() {
        return count;
    }

    /**
     * Returns the length of the buffer, i.e. the internal storage capacity.
     *
     * @return the buffer length.
     */
    public int length() {
        return buffer.length;
    }

    /**
     * Returns the number of bytes available from the current position.
     *
     * @return number of bytes available in the buffer.
     */
    public int available() {
        return count - pos;
    }


    /**
     * Reads the next byte in the buffer and advance the cursor.
     *
     * @return the next byte available.
     */
    public int read() {
        assert pos < count;

        return buffer[pos++];
    }

    /**
     * Reads at most <code>len</code> bytes from the buffer and advance the
     * cursor.
     *
     * @param out    the byte array into which the data is read
     * @param offset the start offset in the destination array out
     * @param len    the maximum number of bytes read
     * @return the actual number of bytesread
     */
    public int read(byte[] out, int offset, int len) {
        assert pos < count;

        int copyLen = Math.min(len, count - pos);

        if (copyLen > 0) {
            System.arraycopy(this.buffer, pos, out, offset, copyLen);
            pos += copyLen;
        }

        return copyLen;
    }

    /**
     * Resets the internal cursor to the beginning of the buffer.
     */
    public void clear() {
        this.pos = this.count = 0;
    }

    /**
     * Writes a byte to the current cursor position in the buffer and advances the cursor
     *
     * @param data byte to be written
     */
    public void put(int data) {
        buffer[count++] = (byte) data;
    }


    /**
     * Copy <code>len</code> number of bytes starting from <code>offset</code> to the current
     * cursor position.
     *
     * @param offset offset from which start copying data
     * @param len    number of bytes to be copied
     */
    public void copyFrom(int offset, int len) {
        while (len-- > 0)
            buffer[count++] = buffer[offset++];
    }

}

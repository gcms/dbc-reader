package com.github.gcms.blast;

import java.io.IOException;

import static com.github.gcms.blast.Blast.*;

/**
 * Decompress data and keep decompression algorithm state.
 */
public class BlastDecoder {
    private final BlastInput input;

    private BlastHeader header; /* header information */

    private boolean first;      /* true to check distances (for first 4K) */

    private int copyDistance;   /* distance for copy */
    private int copyLength;     /* length for copy */


    /**
     * Creates a <code>BlastDecoder</code> for decoding data from a
     * <code>BlastInput</code>.
     *
     * @param input a <code>BlastInput</code> from which data is to be read.
     * @param first <code>true</code> to check distances (for first 4K)
     */
    public BlastDecoder(BlastInput input, boolean first) {
        this.input = input;
        this.first = first;
    }

    /**
     * Decode data from input into a buffer.
     *
     * @param buffer a <code>BlastBuffer</code> where the data is to be read.
     * @return <code>true</code> if there is still more data to be read; false
     * if end of file is detected.
     * @throws IOException          if an I/O error occurs.
     * @throws BlastFormatException if the compressed input is malformed.
     */
    public boolean read(BlastBuffer buffer) throws IOException, BlastFormatException {
        if (header == null)
            header = BlastHeader.read(input);

        if (copyLength > 0) {   // remaining bytes to copy
            copyLength -= copy(buffer, copyLength, copyDistance);
        } else if (input.bits(1) != 0) { /* decode literals and length/distance pairs */
            /* decoded symbol, extra bits for distance */
            int symbol = input.decode(LENCODE);
            int distance = input.bits(EXTRA[symbol]);

            /* get length */
            copyLength = BASE[symbol] + distance;
            if (copyLength == 519)
                return false; /* end code */

            /* get distance */
            symbol = copyLength == 2 ? 2 : header.dictSize;
            copyDistance = input.decode(DISTCODE) << symbol;
            copyDistance += input.bits(symbol);
            copyDistance++;
            if (first && copyDistance > buffer.size())
                throw new BlastFormatException(-3); /* distance too far back */

            /* copy length bytes from distance bytes back */
            copyLength -= copy(buffer, copyLength, copyDistance);
        } else {
            /* get literal and put it */
            int symbol = header.encoded
                    ? input.decode(LITCODE)
                    : input.bits(8); /* decoded symbol, extra bits for distance */

            buffer.put(symbol);
        }

        if (buffer.isFull())
            first = false;

        return true;
    }

    private static int copy(BlastBuffer buffer, int copyLength, int copyDistance) {
        int bufferSize = buffer.size();

        int from = bufferSize - copyDistance;

        /* copy counter */
        int copyCounter = buffer.length();
        if (bufferSize < copyDistance) {
            from += copyCounter;
            copyCounter = copyDistance;
        }

        copyCounter -= bufferSize;
        if (copyCounter > copyLength)
            copyCounter = copyLength;

        buffer.copyFrom(from, copyCounter);

        return copyCounter;
    }

}

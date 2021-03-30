package com.github.gcms.blast;

import java.io.IOException;

/**
 * Class to decode and keep blast header information.
 */
class BlastHeader {
    final boolean encoded;    /* true if literals are coded */
    final int dictSize;       /* log2(dictionary size) - 6 */

    BlastHeader(boolean encoded, int dictSize) {
        this.encoded = encoded;
        this.dictSize = dictSize;
    }

    /**
     * Read header information from a BlastInput.
     *
     * @param input a <code>BlastInput</code> from which to read.
     * @return a new <code>BlastHeader</code>.
     * @throws IOException          if an I/O error occurs.
     * @throws BlastFormatException if the compressed input is malformed.
     */
    static BlastHeader read(BlastInput input) throws IOException, BlastFormatException {
        int lit = input.bits(8);
        if (lit > 1)
            throw new BlastFormatException(-1);

        int dict = input.bits(8);
        if (dict < 4 || dict > 6)
            throw new BlastFormatException(-2);


        return new BlastHeader(lit != 0, dict);
    }
}

package com.github.gcms.blast;


import static com.github.gcms.blast.Blast.MAX_CODE_LEN;

/**
 * Class to represent a Huffman table.
 */
public class Huffman {

    short[] m_count;
    short[] m_symbol;

    /**
     * Constructor.
     *
     * @param countSize  number of counts
     * @param symbolSize number of symbols
     * @param rep        repeated code lengths
     */
    public Huffman(int countSize, int symbolSize, int[] rep) {
        m_count = new short[countSize];
        m_symbol = new short[symbolSize];
        construct(rep, rep.length);
    }



    /**
     * Given a list of repeated code lengths rep[0..n-1], where each byte is a
     * count (high four bits + 1) and a code length (low four bits), generate the
     * list of code lengths.  This compaction reduces the size of the object code.
     * Then given the list of code lengths length[0..n-1] representing a canonical
     * Huffman code for n symbols, construct the tables required to decode those
     * codes.  Those tables are the number of codes of each length, and the symbols
     * sorted by length, retaining their original order within each length.  The
     * return value is zero for a complete code set, negative for an over-
     * subscribed code set, and positive for an incomplete code set.  The tables
     * can be used if the return value is zero or positive, but they cannot be used
     * if the return value is negative.  If the return value is zero, it is not
     * possible for decode() using that table to return an error--any stream of
     * enough bits will resolve to a symbol.  If the return value is positive, then
     * it is possible for decode() using that table to return an error for received
     * codes past the end of the incomplete lengths.
     *
     * @param rep repeated code lengths
     * @param n number of repeated codes
     */
    private void construct(int[] rep, int n) {
        int symbol; /* current symbol when stepping through length[] */
        int len; /* current length when stepping through h->count[] */
        int left; /* number of possible codes left of current length */
        short[] offs = new short[MAX_CODE_LEN + 1]; /* offsets in symbol table for each length */
        short[] length = new short[256]; /* code lengths */

        /* convert compact repeat counts into symbol bit length list */
        symbol = 0;
        int repIndex = 0;
        do {
            len = rep[repIndex++];
            left = (len >> 4) + 1;
            len &= 15;
            do {
                length[symbol++] = (short) len;
            }
            while (--left != 0);
        }
        while (--n != 0);
        n = symbol;

        /* count number of codes of each length */
        for (len = 0; len <= MAX_CODE_LEN; len++) {
            m_count[len] = 0;
        }

        for (symbol = 0; symbol < n; symbol++) {
            (m_count[length[symbol]])++; /* assumes lengths are within bounds */
        }

        if (m_count[0] == n) /* no codes! */ {
            return; /* complete, but decode() will fail */
        }

        /* check for an over-subscribed or incomplete set of lengths */
        left = 1; /* one possible code of zero length */
        for (len = 1; len <= MAX_CODE_LEN; len++) {
            left <<= 1; /* one more bit, double codes left */
            left -= m_count[len]; /* deduct count from possible codes */
            if (left < 0) {
                return; /* over-subscribed--return negative */
            }
        } /* left > 0 means incomplete */

        /* generate offsets into symbol table for each length for sorting */
        offs[1] = 0;
        for (len = 1; len < MAX_CODE_LEN; len++) {
            offs[len + 1] = (short) (offs[len] + m_count[len]);
        }

        /*
         * put symbols in table sorted by length, by symbol order within each
         * length
         */
        for (symbol = 0; symbol < n; symbol++) {
            if (length[symbol] != 0) {
                m_symbol[offs[length[symbol]]++] = (short) symbol;
            }
        }

        /* return zero for complete set, positive for incomplete set */
    }
}

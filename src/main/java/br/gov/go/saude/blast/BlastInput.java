package br.gov.go.saude.blast;

import java.io.IOException;
import java.io.InputStream;

import static br.gov.go.saude.blast.Blast.MAX_CODE_LEN;

/**
 * Class for reading bits and decoding the input using Huffman tables.
 * <p>
 * This is refactoring from the code available in the following sources:
 * <p>
 * https://github.com/madler/zlib/blob/master/contrib/blast/blast.c
 * https://github.com/aminea7/GraphicalUserInterface/blob/master/mpxj/src/main/java/net/sf/mpxj/primavera/common/Blast.java
 */
public class BlastInput {
    private int m_bitbuf;   /* bit buffer */
    private int m_bitcnt;   /* number of bits in bit buffer */

    private int m_left;     /* available input at in */
    private int m_in;       /* read input location */

    private InputStream m_input;    /* input stream provided by user */

    public BlastInput(InputStream m_input) {
        this.m_input = m_input;
    }

    /**
     * Return need bits from the input stream.  This always leaves less than
     * eight bits in the buffer.  bits() works properly for need == 0.
     * <p>
     * Format notes:
     * <p>
     * - Bits are stored in bytes from the least significant bit to the most
     * significant bit.  Therefore bits are dropped from the bottom of the bit
     * buffer, using shift right, and new bytes are appended to the top of the
     * bit buffer, using shift left.
     *
     * @param need number of bits required
     * @return bit values
     * @throws IOException          if an I/O error occurs.
     * @throws BlastFormatException if the compressed input is malformed.
     */
    public int bits(int need) throws IOException, BlastFormatException {
        int val; /* bit accumulator */

        /* load at least need bits into val */
        val = m_bitbuf;
        while (m_bitcnt < need) {
            if (m_left == 0) {
                m_in = m_input.read();
                m_left = m_in == -1 ? 0 : 1;
                if (m_left == 0) {
                    throw new BlastFormatException(2); /* out of input */
                }
            }
            val |= m_in << m_bitcnt; /* load eight bits */
            m_left--;
            m_bitcnt += 8;
        }

        /* drop need bits and update buffer, always zero to seven bits left */
        m_bitbuf = val >> need;
        m_bitcnt -= need;

        /* return need bits, zeroing the bits above that */
        return val & ((1 << need) - 1);
    }

    /**
     * Decode a code from the stream s using huffman table h.  Return the symbol or
     * a negative value if there is an error.  If all of the lengths are zero, i.e.
     * an empty code, or if the code is incomplete and an invalid code is received,
     * then -9 is returned after reading MAX_CODE_LEN bits.
     * <p>
     * Format notes:
     * <p>
     * - The codes as stored in the compressed data are bit-reversed relative to
     * a simple integer ordering of codes of the same lengths.  Hence below the
     * bits are pulled from the compressed data one at a time and used to
     * build the code value reversed from what is in the stream in order to
     * permit simple integer comparisons for decoding.
     * <p>
     * - The first code for the shortest length is all ones.  Subsequent codes of
     * the same length are simply integer decrements of the previous code.  When
     * moving up a length, a one bit is appended to the code.  For a complete
     * code, the last code of the longest length will be all zeros.  To support
     * this ordering, the bits pulled during decoding are inverted to apply the
     * more "natural" ordering starting with all zeros and incrementing.
     *
     * @param h Huffman table
     * @return status code
     * @throws IOException          if an I/O error occurs.
     * @throws BlastFormatException if the compressed input is malformed.
     */
    public int decode(Huffman h) throws IOException, BlastFormatException {
        int len; /* current number of bits in code */
        int code; /* len bits being decoded */
        int first; /* first code of length len */
        int count; /* number of codes of length len */
        int index; /* index of first code of length len in symbol table */
        int bitbuf; /* bits from stream */
        int left; /* bits left in read or left to process */
        //short *read;        /* read number of codes */

        bitbuf = m_bitbuf;
        left = m_bitcnt;
        code = first = index = 0;
        len = 1;
        int nextIndex = 1; // read = h->count + 1;
        while (true) {
            while (left-- != 0) {
                code |= (bitbuf & 1) ^ 1; /* invert code */
                bitbuf >>= 1;
                //count = *read++;
                count = h.m_count[nextIndex++];
                if (code < first + count) { /* if length len, return symbol */
                    m_bitbuf = bitbuf;
                    m_bitcnt = (m_bitcnt - len) & 7;
                    return h.m_symbol[index + (code - first)];
                }
                index += count; /* else update for read length */
                first += count;
                first <<= 1;
                code <<= 1;
                len++;
            }
            left = (MAX_CODE_LEN + 1) - len;
            if (left == 0) {
                break;
            }
            if (m_left == 0) {
                m_in = m_input.read();
                m_left = m_in == -1 ? 0 : 1;
                if (m_left == 0) {
                    throw new BlastFormatException(2); /* out of input */
                }
            }
            bitbuf = m_in;
            m_left--;
            if (left > 8) {
                left = 8;
            }
        }
        return -9; /* ran out of codes */
    }
}

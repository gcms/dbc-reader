package br.gov.go.saude.blast;
/*
 * Blast is a class which handles decompression of data compressed using
 * the PKWare Compression Library. (Blast being an alternative to explode -
 * the name of the original PKWare decompression routine).
 *
 * This is an adaptation from the code available in the following sources:
 *
 * https://github.com/madler/zlib/blob/master/contrib/blast/blast.c
 * https://github.com/aminea7/GraphicalUserInterface/blob/master/mpxj/src/main/java/net/sf/mpxj/primavera/common/Blast.java
 *

 * I have refactored the code to make it usable as a Java InputStream on a
 * read by byte/byte array basis instead of the sequential decompressing
 * procedure.
 *
 * For some parts, notably the Huffman table and input reading I kept the
 * variable names and comments, but refactored to a OO approach.
 */

/* blast.h -- interface for blast.c
  Copyright (C) 2003, 2012, 2013 Mark Adler
  version 1.3, 24 Aug 2013
  This software is provided 'as-is', without any express or implied
  warranty.  In no event will the author be held liable for any damages
  arising from the use of this software.
  Permission is granted to anyone to use this software for any purpose,
  including commercial applications, and to alter it and redistribute it
  freely, subject to the following restrictions:
  1. The origin of this software must not be misrepresented; you must not
     claim that you wrote the original software. If you use this software
     in a product, an acknowledgment in the product documentation would be
     appreciated but is not required.
  2. Altered source versions must be plainly marked as such, and must not be
     misrepresented as being the original software.
  3. This notice may not be removed or altered from any source distribution.
  Mark Adler    madler@alumni.caltech.edu
 */

/*
 * blast() decompresses the PKWare Data Compression Library (DCL) compressed
 * format.  It provides the same functionality as the explode() function in
 * that library.  (Note: PKWare overused the "implode" verb, and the format
 * used by their library implode() function is completely different and
 * incompatible with the implode compression method supported by PKZIP.)
 *
 * The binary mode for stdio functions should be used to assure that the
 * compressed data is not corrupted when read or written.  For example:
 * fopen(..., "rb") and fopen(..., "wb").
 */

/* Decompress input to output using the provided infun() and outfun() calls.
 * On success, the return value of blast() is zero.  If there is an error in
 * the source data, i.e. it is not in the proper format, then a negative value
 * is returned.  If there is not enough input available or there is not enough
 * output space, then a positive error is returned.
 *
 * The input function is invoked: len = infun(how, &buf), where buf is set by
 * infun() to point to the input buffer, and infun() returns the number of
 * available bytes there.  If infun() returns zero, then blast() returns with
 * an input error.  (blast() only asks for input if it needs it.)  inhow is for
 * use by the application to pass an input descriptor to infun(), if desired.
 *
 * If left and in are not NULL and *left is not zero when blast() is called,
 * then the *left bytes are *in are consumed for input before infun() is used.
 *
 * The output function is invoked: err = outfun(how, buf, len), where the bytes
 * to be written are buf[0..len-1].  If err is not zero, then blast() returns
 * with an output error.  outfun() is always called with len <= 4096.  outhow
 * is for use by the application to pass an output descriptor to outfun(), if
 * desired.
 *
 * If there is any unused input, *left is set to the number of bytes that were
 * read and *in points to them.  Otherwise *left is set to zero and *in is set
 * to NULL.  If left or in are NULL, then they are not set.
 *
 * The return codes are:
 *
 *   2:  ran out of input before completing decompression
 *   1:  output error before completing decompression
 *   0:  successful decompression
 *  -1:  literal flag not zero or one
 *  -2:  dictionary size not in 4..6
 *  -3:  distance is too far back
 *
 * At the bottom of blast.c is an example program that uses blast() that can be
 * compiled to produce a command-line decompression filter by defining TEST.
 */

/* blast.c
 * Copyright (C) 2003, 2012, 2013 Mark Adler
 * For conditions of distribution and use, see copyright notice in blast.h
 * version 1.3, 24 Aug 2013
 *
 * blast.c decompresses data compressed by the PKWare Compression Library.
 * This function provides functionality similar to the explode() function of
 * the PKWare library, hence the name "blast".
 *
 * This decompressor is based on the excellent format description provided by
 * Ben Rudiak-Gould in comp.compression on August 13, 2001.  Interestingly, the
 * example Ben provided in the post is incorrect.  The distance 110001 should
 * instead be 111000.  When corrected, the example byte stream becomes:
 *
 *    00 04 82 24 25 8f 80 7f
 *
 * which decompresses to "AIAIAIAIAIAIA" (without the quotes).
 */


public class Blast {
    public static final int MAX_CODE_LEN = 13;   /* maximum code length */

    /* bit lengths of literal codes */
    private static final int[] LITLEN =
            {
                    11,
                    124,
                    8,
                    7,
                    28,
                    7,
                    188,
                    13,
                    76,
                    4,
                    10,
                    8,
                    12,
                    10,
                    12,
                    10,
                    8,
                    23,
                    8,
                    9,
                    7,
                    6,
                    7,
                    8,
                    7,
                    6,
                    55,
                    8,
                    23,
                    24,
                    12,
                    11,
                    7,
                    9,
                    11,
                    12,
                    6,
                    7,
                    22,
                    5,
                    7,
                    24,
                    6,
                    11,
                    9,
                    6,
                    7,
                    22,
                    7,
                    11,
                    38,
                    7,
                    9,
                    8,
                    25,
                    11,
                    8,
                    11,
                    9,
                    12,
                    8,
                    12,
                    5,
                    38,
                    5,
                    38,
                    5,
                    11,
                    7,
                    5,
                    6,
                    21,
                    6,
                    10,
                    53,
                    8,
                    7,
                    24,
                    10,
                    27,
                    44,
                    253,
                    253,
                    253,
                    252,
                    252,
                    252,
                    13,
                    12,
                    45,
                    12,
                    45,
                    12,
                    61,
                    12,
                    45,
                    44,
                    173
            };

    /* bit lengths of length codes 0..15 */
    private static final int[] LENLEN =
            {
                    2,
                    35,
                    36,
                    53,
                    38,
                    23
            };

    /* bit lengths of distance codes 0..63 */
    private static final int[] DISTLEN =
            {
                    2,
                    20,
                    53,
                    230,
                    247,
                    151,
                    248
            };

    static final short[] BASE =
            { /* base for length codes */
                    3,
                    2,
                    4,
                    5,
                    6,
                    7,
                    8,
                    9,
                    10,
                    12,
                    16,
                    24,
                    40,
                    72,
                    136,
                    264
            };

    /* extra bits for length codes */
    static final int[] EXTRA =
            {
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8
            };

    static final Huffman LITCODE = new Huffman(MAX_CODE_LEN + 1, 256, LITLEN); /* length code */
    static final Huffman LENCODE = new Huffman(MAX_CODE_LEN + 1, 16, LENLEN); /* length code */
    static final Huffman DISTCODE = new Huffman(MAX_CODE_LEN + 1, 64, DISTLEN);/* distance code */
}

package com.github.gcms.blast;

import java.io.IOException;

/**
 * Signals a malformed Blast input.
 */
public class BlastFormatException extends IOException {
    private static final String[] ERROR_MESSAGES = new String[]{
            "-3: distance is too far back",
            "-2: dictionary size not in 4..6",
            "-1: literal flag not zero or one",
            "0: successful decompression",
            "1: output error before completing decompression",
            "2: ran out of input before completing decompression"
    };

    private final int code;

    BlastFormatException(int code) {
        super(String.format("Couldn't explode/blast file. %s", ERROR_MESSAGES[code + 3]));
        this.code = code;
    }

    /**
     * Returns the error code as follows.
     * <p>
     *  2:  ran out of input before completing decompression
     *  1:  output error before completing decompression
     * -1:  literal flag not zero or one
     * -2:  dictionary size not in 4..6
     * -3:  distance is too far back
     *
     * @return the error code which generated the exception.
     */
    public int getCode() {
        return this.code;
    }
}

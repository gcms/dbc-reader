package br.gov.go.saude.dbc;


import br.gov.go.saude.blast.BlastInputStream;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;

/**
 * Implements a stream for reading DBC files, xBase files compressed in
 * PKWare Data Compression Library (DCL) compressed format
 */
public class DBCInputStream extends FilterInputStream {
    /**
     * Creates a new InputStream from the underlying stream
     *
     * @param is the input stream
     */
    public DBCInputStream(InputStream is) {
        super(new SequenceInputStream(new DBCHeaderReader(is), new BlastInputStream(is)));
    }

}

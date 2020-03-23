package br.gov.go.saude.dbc;

import java.io.IOException;

/**
 * Signals a malformed DBC input.
 */
public class DBCFormatException extends IOException {

    DBCFormatException(String message) {
        super(message);
    }

    DBCFormatException(String message, Exception e) {
        super(message, e);
    }
}

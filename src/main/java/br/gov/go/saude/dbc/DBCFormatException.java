package br.gov.go.saude.dbc;

import java.io.IOException;

public class DBCFormatException extends IOException {

  public DBCFormatException(int code) {
    super(String.format("Couldn't explode/blast file. See blast.c [exit code: %d]", code));
  }

  public DBCFormatException(String message) {
    super(message);
  }
}

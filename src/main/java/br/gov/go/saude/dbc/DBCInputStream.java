package br.gov.go.saude.dbc;


import java.io.*;

public class DBCInputStream extends InputStream {
  private final int bufferSize;
  private final InputStream source;
  private InputStream wrapper;

  private static final int DEFAULT_BUFFER_SIZE = 1024 / 2;

  public DBCInputStream(InputStream is) {
    this(is, DEFAULT_BUFFER_SIZE);
  }

  public DBCInputStream(InputStream is, int bufferSize) {
    this.source = is;
    this.bufferSize = bufferSize;
  }

  private IOException exception;

  private synchronized void setException(IOException ex) {
    this.exception = ex;
  }

  private synchronized IOException getException() {
    return exception;
  }


  @Override
  public int read() throws IOException {
    if (wrapper == null)
      wrapper = startProcessing();

    if (getException() != null)
      throw getException();

    return wrapper.read();
  }

  private InputStream startProcessing() throws IOException {
    PipedInputStream pis = new PipedInputStream(bufferSize);
    PipedOutputStream pos = new PipedOutputStream(pis);

    Thread th = new Thread(() -> {
      try {
        new DBCProcessor(source, pos).decompress();

      } catch (IOException e) {
        setException(e);
      } finally {
        try {
          pos.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });

    th.start();
    return pis;
  }

}

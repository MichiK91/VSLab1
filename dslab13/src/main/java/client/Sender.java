package client;

import java.io.IOException;

public interface Sender {
  public void send(Object req) throws IOException;
  public Object receive(); 
  public void close() throws IOException;
}

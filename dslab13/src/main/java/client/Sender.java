package client;

import java.io.IOException;
import java.net.UnknownHostException;

import message.Request;
import message.Response;

public interface Sender {
  public void send(Object req) throws IOException;
  public Object receive(); 
  public void close() throws IOException;
}

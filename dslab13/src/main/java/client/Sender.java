package client;

import java.io.IOException;
import java.net.UnknownHostException;

import message.Request;
import message.Response;

public interface Sender {
  public void send(Request req) throws IOException;
  public Response receive(); 
  public void close() throws IOException;
}

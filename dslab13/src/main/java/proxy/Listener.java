package proxy;

import java.io.IOException;


public interface Listener {
  public void listen(Object req) throws IOException;
  public Object receive(); 
  public void close() throws IOException;
}


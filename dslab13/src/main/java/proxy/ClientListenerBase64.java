package proxy;

import java.io.IOException;

import message.MessageWrapper;

import org.bouncycastle.util.encoders.Base64;

public class ClientListenerBase64 implements Listener{

  ClientListenerTCP clientListenerTCP;
  
  public ClientListenerBase64(ClientListenerTCP clientListenerTCP){
    this.clientListenerTCP = clientListenerTCP;
  }
  
  @Override
  public void listen(Object req) throws IOException {
    
    if(req instanceof byte[]){
      byte[] encodedBytes = Base64.encode((byte[])req);
      clientListenerTCP.listen(encodedBytes); 
    } else if (req instanceof MessageWrapper){

      byte[] yourBytes = ((MessageWrapper) req).getContent();
      byte[] encodedBytes = Base64.encode(yourBytes);
      clientListenerTCP.listen(new MessageWrapper(encodedBytes, ((MessageWrapper) req).isMessage()));
      
    }
  }

  @Override
  public Object receive() {
    Object o = this.clientListenerTCP.receive();
    if(o instanceof byte[]){
      System.out.println("base 64 byte[] received "+o);
      byte[] req = Base64.decode((byte[])o);
      System.out.println(req);
      return req;
    } else if (o instanceof MessageWrapper){
      System.out.println("base 64 mw received "+o.getClass());
      return new MessageWrapper(Base64.decode(((MessageWrapper) o).getContent()),((MessageWrapper) o).isMessage());
    }
    return null;
  }

  @Override
  public void close() throws IOException {
    // TODO Auto-generated method stub
    
  }

  public ClientListenerTCP getListener(){
    return this.clientListenerTCP;
  }
}

package client;

import java.io.IOException;

import message.MessageWrapper;

import org.bouncycastle.util.encoders.Base64;


public class ProxySenderBase64 implements Sender {
  ProxySenderTCP proxySender;
  
  public ProxySenderBase64(ProxySenderTCP proxySender){
    this.proxySender = proxySender;
  }
  
  @Override
  public void send(Object req) throws IOException {
    System.out.println("send object base64 " +req.getClass());
    System.out.println("send byte[] base64 connected" +req);
    if(req instanceof byte[]){
      System.out.println("send byte[] base64 " +req);
      byte[] encodedBytes = Base64.encode((byte[])req);
      proxySender.send(encodedBytes); 
    } else if (req instanceof MessageWrapper){
      System.out.println("send message base64 " +req);
      byte[] yourBytes = ((MessageWrapper) req).getContent();
      byte[] encodedBytes = Base64.encode(yourBytes);
      proxySender.send(new MessageWrapper(encodedBytes, ((MessageWrapper) req).isMessage()));
      
    }
  }

  @Override
  public void close() throws IOException {
    proxySender.close();
  }

  @Override
  public Object receive() {
    
    Object res = proxySender.receive();
    
    if(res instanceof byte[]){
      System.out.println("receive Object base "+res);
      return Base64.decode((byte[])res);
    } else if(res instanceof MessageWrapper){
      System.out.println("receive Object base "+res);
      byte[] encodedBytes = ((MessageWrapper) res).getContent();
      return new MessageWrapper(Base64.decode(encodedBytes), ((MessageWrapper) res).isMessage());
    }
    return res;
  }

}

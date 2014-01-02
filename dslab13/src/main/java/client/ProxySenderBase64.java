package client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.security.SecureRandom;

import message.MessageWrapper;
import message.Request;
import message.request.LoginRequest;
import message.Response;
import org.bouncycastle.util.encoders.Base64;


public class ProxySenderBase64 implements Sender {
  ProxySenderTCP proxySender;
  
  public ProxySenderBase64(ProxySenderTCP proxySender){
    this.proxySender = proxySender;
  }
  
  @Override
  public void send(Object req) throws IOException {
    proxySender.connect();
    
    if(req instanceof byte[]){
      byte[] encodedBytes = Base64.encode((byte[])req);
      proxySender.send(encodedBytes); 
    } else {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutput out = null;
      try {
        out = new ObjectOutputStream(bos);   
        out.writeObject(req);
        byte[] yourBytes = bos.toByteArray();
        byte[] encodedBytes = Base64.encode(yourBytes);
        proxySender.send(new MessageWrapper(encodedBytes));
      } finally {
        try {
          if (out != null) {
            out.close();
          }
        } catch (IOException ex) {
          // ignore close exception
        }
        try {
          bos.close();
        } catch (IOException ex) {
          // ignore close exception
        }
      }
      proxySender.send(req);
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
      return Base64.decode((byte[])res);
    } else if(res instanceof MessageWrapper){
      byte[] encodedBytes = ((MessageWrapper) res).getContent();
      return new MessageWrapper(Base64.decode(encodedBytes));
    }
    return res;
  }

}

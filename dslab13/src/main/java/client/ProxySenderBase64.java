package client;

import java.io.IOException;
import java.security.SecureRandom;

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
  public void send(Request req) throws IOException {
    proxySender.connect();
    
    if(req instanceof LoginRequest){
      String username = ((LoginRequest)req).getUsername();
      SecureRandom secureRandom = new SecureRandom(); 
      final byte[] number = new byte[32]; 
      secureRandom.nextBytes(number);
      String clientChallenge = new String(secureRandom.generateSeed(32));
      proxySender.strout.writeChars("!login " + username + " " + Base64.encode(clientChallenge.getBytes()));
    } else {
      //TODO encode base64
      proxySender.send(req);
    }
  }

  @Override
  public void close() throws IOException {
    proxySender.close();
  }

  @Override
  public Response receive() {
    Response res = proxySender.receive();
    
    //TODO decode Base64
    return res;
  }

}

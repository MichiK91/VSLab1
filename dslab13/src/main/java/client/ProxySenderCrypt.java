package client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import message.Request;
import message.Response;
import message.request.LoginRequest;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.openssl.PEMReader;


public class ProxySenderCrypt implements Sender {

  String user;
  ProxySenderBase64 proxySender;
  
  public ProxySenderCrypt(ProxySenderBase64 proxySender){
    this.proxySender = proxySender;
  }
  
  @Override
  public void send(Request req) throws IOException {

    Cipher cryptCipher = null;
    
    if(req instanceof LoginRequest){
      try {
        cryptCipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
      } catch (NoSuchAlgorithmException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (NoSuchPaddingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } else{
      try {
        cryptCipher = Cipher.getInstance("AES/CTR/NoPadding");
      } catch (NoSuchAlgorithmException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (NoSuchPaddingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }  
    
    String pathToPublicKey = "keys/proxy.pem";
    PEMReader pemReader = new PEMReader(new FileReader(pathToPublicKey)); 
    PublicKey publicKey = (PublicKey) pemReader.readObject();
        
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutput out = null;
    try {
      out = new ObjectOutputStream(bos);   
      out.writeObject(req);
      byte[] yourBytesReq = bos.toByteArray();
      
      cryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
      byte[] encryptReq = cryptCipher.doFinal(yourBytesReq);
      
      ByteArrayInputStream bis = new ByteArrayInputStream(encryptReq);
      ObjectInput in = null;
      try {
        in = new ObjectInputStream(bis);
        Request o = (Request) in.readObject(); 

        proxySender.send(o);
        
      } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } finally {
        try {
          bis.close();
        } catch (IOException ex) {
          // ignore close exception
        }
        try {
          if (in != null) {
            in.close();
          }
        } catch (IOException ex) {
          // ignore close exception
        }
      }
      
    } catch (InvalidKeyException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalBlockSizeException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (BadPaddingException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
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

    
  }

  @Override
  public void close() throws IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public Response receive() {
    Response req = proxySender.receive();
    // TODO encrypt "RSA/NONE/OAEPWithSHA256AndMGF1Padding" or "AES/CTR/NoPadding"
    return req;
  }

}

package client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import message.MessageWrapper;
import message.Request;
import message.Response;
import message.request.LoginRequest;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.bouncycastle.openssl.PEMReader;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

import util.Config;


public class ProxySenderCrypt implements Sender {

  String user;
  ProxySenderBase64 proxySender;
  PublicKey publicKey;
  PrivateKey privateKey;
  SecretKey AESKey;
  
  public ProxySenderCrypt(ProxySenderBase64 proxySender){
    this.proxySender = proxySender;
    Config config = new Config("client");
    config.getString("keys.dir");
    String pathToPublicKey = config.getString("proxy.key");
    PEMReader pemReader;
    try {
      pemReader = new PEMReader(new FileReader(pathToPublicKey));
      PublicKey publicKey = (PublicKey) pemReader.readObject();
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } 
    
  }
  
  @Override
  public void send(Object req) throws IOException {

    Cipher cryptCipher = null;
    
    if(req instanceof String){
      try {
        
        Config config = new Config("client");
        config.getString("keys.dir");
        String[] parts = ((String)req).split(" ");
        if(parts.length>1){
          String pathToPrivateKey = config.getString("keys.dir/"+parts[1]);
          PEMReader pemReader;
          try {
            pemReader = new PEMReader(new FileReader(pathToPrivateKey));
            privateKey = (PrivateKey) pemReader.readObject();
          } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          
          cryptCipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
          cryptCipher.init(Cipher.ENCRYPT_MODE, AESKey);
          
          byte[] encryptReq = cryptCipher.doFinal(((String)req).getBytes());
          proxySender.send(encryptReq);
        } else {
          cryptCipher = Cipher.getInstance("AES/CTR/NoPadding");
          cryptCipher.init(Cipher.ENCRYPT_MODE, publicKey); //TODO iv-parameter
          byte[] encryptReq = cryptCipher.doFinal(((String)req).getBytes());
          proxySender.send(encryptReq);
        }
        
      } catch (NoSuchAlgorithmException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (NoSuchPaddingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (InvalidKeyException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IllegalBlockSizeException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (BadPaddingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } else{
      try {
        cryptCipher = Cipher.getInstance("AES/CTR/NoPadding");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;

        out = new ObjectOutputStream(bos);   
        out.writeObject(req);
        byte[] yourBytesReq = bos.toByteArray();
        
        cryptCipher.init(Cipher.ENCRYPT_MODE, AESKey);
        byte[] encryptReq = cryptCipher.doFinal(yourBytesReq);
        
        ByteArrayInputStream bis = new ByteArrayInputStream(encryptReq);
        
        ObjectInput in = null;
        in = new ObjectInputStream(bis);
        Request o = (Request) in.readObject(); 

        proxySender.send(o);
          
        } catch (NoSuchAlgorithmException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (NoSuchPaddingException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (InvalidKeyException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (BadPaddingException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (ClassNotFoundException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      } 
    
  }

  @Override
  public void close() throws IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public Object receive(){
    Object req = proxySender.receive();
    if(req instanceof byte[]){
      Cipher cryptCipher;
      try {
        cryptCipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
        cryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
        
        byte[] decryptReq = cryptCipher.doFinal((byte[])req);
        String answer = new String(decryptReq);
        String[] answerSplit = answer.split(" ");
        if(Base64.decode(answerSplit[0].getBytes()).equals("!ok")){
          try {
            this.send(answerSplit[2]);
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | Base64DecodingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } else if(req instanceof MessageWrapper){
      
      Cipher cryptCipher;
      try {
        cryptCipher = Cipher.getInstance("AES/CTR/NoPadding");
        //TODO iv-parameter
        cryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
        
        byte[] decryptReq = cryptCipher.doFinal(((MessageWrapper)req).getContent());
        return new MessageWrapper(decryptReq);
      } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
        e.printStackTrace();
      }
    }
    return req;
  }

}

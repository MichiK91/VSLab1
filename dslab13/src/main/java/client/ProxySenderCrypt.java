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
import message.response.LoginResponse;

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
  byte[] ivParameter;
  
  public ProxySenderCrypt(ProxySenderBase64 proxySender){
    this.proxySender = proxySender;
    Config config = new Config("client");
    String pathToPublicKey = config.getString("proxy.key");
    PEMReader pemReader;
    try {
      pemReader = new PEMReader(new FileReader(pathToPublicKey));
      publicKey = (PublicKey) pemReader.readObject();
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
        String keysDir = config.getString("keys.dir");
        String[] parts = ((String)req).split(" ");
        if(parts.length>1){
          String pathToPrivateKey = config.getString(keysDir+"/"+parts[1]+".pem");
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
          cryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
          
          byte[] encryptReq = cryptCipher.doFinal(((String)req).getBytes());
          proxySender.send(encryptReq);
        } else {
          cryptCipher = Cipher.getInstance("AES/CTR/NoPadding");
          cryptCipher.init(Cipher.ENCRYPT_MODE, AESKey); //TODO iv-parameter
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
        
         

        proxySender.send(new MessageWrapper(encryptReq));
          
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
        if(answerSplit[0].getBytes().equals("!ok")){
          try {
            this.send(answerSplit[2]);
            return new LoginResponse(LoginResponse.Type.SUCCESS);
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
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
        ByteArrayInputStream bis = new ByteArrayInputStream(decryptReq);
        ObjectInput in = null;
        try {
          in = new ObjectInputStream(bis);
          Object o = in.readObject(); 
          if(o instanceof Response){
            return o;
          }
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
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
        return new MessageWrapper(decryptReq);
      } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
        e.printStackTrace();
      }
    }
    return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS);
  }

}

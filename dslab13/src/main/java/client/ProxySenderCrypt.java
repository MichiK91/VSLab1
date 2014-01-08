package client;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import message.MessageWrapper;
import message.Request;
import message.Response;
import message.response.MessageResponse;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.util.encoders.Base64;

import util.Config;
import util.Serializer;


public class ProxySenderCrypt implements Sender {

  String user;
  ProxySenderBase64 proxySender;
  PublicKey publicKey;
  PrivateKey privateKey;
  SecretKey AESKey;
  byte[] ivParameter;
  private byte[] clientChallenge;
  
  public ProxySenderCrypt(ProxySenderBase64 proxySender){
    
    this.proxySender = proxySender;
    Config config = new Config("client");
    String pathToPublicKey = config.getString("proxy.key");
    PEMReader pemReader;
    try {
      pemReader = new PEMReader(new FileReader(pathToPublicKey));
      publicKey = (PublicKey) pemReader.readObject();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
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
        if(parts.length==3){
          
          String pathToPrivateKey = keysDir+"/"+parts[1]+".pem";

          PEMReader pemReader = null;
          try {
            final String pw = parts[2];
            pemReader = new PEMReader(new FileReader(pathToPrivateKey), new PasswordFinder() {

              @Override
              public char[] getPassword() {
              
                return pw.toCharArray();
                
              }
            });
            KeyPair keyPair = (KeyPair) pemReader.readObject(); 
            privateKey = keyPair.getPrivate();
          } catch (FileNotFoundException e) {
            e.printStackTrace();
          } catch (IOException e) {
            e.printStackTrace();
          } finally {
            if(pemReader != null){
              pemReader.close();
            }
          }

          cryptCipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
          cryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
          SecureRandom secureRandom = new SecureRandom(); 
          final byte[] number = new byte[32]; 
          secureRandom.nextBytes(number);
          clientChallenge = secureRandom.generateSeed(32);
          
          String message = (parts[0]+" "+parts[1]+ " " + new String(Base64.encode(clientChallenge)));
          final String B64 = "a-zA-Z0-9/+";
          assert message.matches("!login \\w+ ["+B64+"]{43}=") : "1st message";
          byte[] encryptReq = cryptCipher.doFinal(message.getBytes());

          proxySender.send(encryptReq);
          
        } else {
          cryptCipher = Cipher.getInstance("AES/CTR/NoPadding");
          SecureRandom secure = new SecureRandom(ivParameter);
          secure.nextBytes(new byte[16]);
          cryptCipher.init(Cipher.ENCRYPT_MODE, AESKey, secure);
          byte[] encryptReq = cryptCipher.doFinal(((String)req).getBytes());
          proxySender.send(new MessageWrapper(encryptReq,false));
        }
        
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      } catch (NoSuchPaddingException e) {
        e.printStackTrace();
      } catch (InvalidKeyException e) {
        
        e.printStackTrace();
      } catch (IllegalBlockSizeException e) {
        
        e.printStackTrace();
      } catch (BadPaddingException e) {
        
        e.printStackTrace();
      }
    } else if (req instanceof Request){
      try {
        cryptCipher = Cipher.getInstance("AES/CTR/NoPadding");
        byte[] yourBytesReq = Serializer.serialize(req);
        cryptCipher.init(Cipher.ENCRYPT_MODE, AESKey, new IvParameterSpec(ivParameter));
        byte[] encryptReq = cryptCipher.doFinal(yourBytesReq);
        
        proxySender.send(new MessageWrapper(encryptReq, true));
          
        } catch (NoSuchAlgorithmException e) {
          
          e.printStackTrace();
        } catch (NoSuchPaddingException e) {
          
          e.printStackTrace();
        } catch (InvalidKeyException e) {
          
          e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
          
          e.printStackTrace();
        } catch (BadPaddingException e) {
          
          e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
          
          e.printStackTrace();
        }
      }else if(req instanceof MessageWrapper){
        try {
          final String B64 = "a-zA-Z0-9/+";
          assert (new String(((MessageWrapper) req).getContent())).matches("["+B64+"]{43}=") : "3rd message";
          cryptCipher = Cipher.getInstance("AES/CTR/NoPadding");

          cryptCipher.init(Cipher.ENCRYPT_MODE, AESKey, new IvParameterSpec(ivParameter));
          byte[] encryptReq = cryptCipher.doFinal(((MessageWrapper) req).getContent());
          
          proxySender.send(new MessageWrapper(encryptReq, ((MessageWrapper) req).isMessage()));
        
        } catch (NoSuchAlgorithmException e) {
          
          e.printStackTrace();
        } catch (NoSuchPaddingException e) {
          
          e.printStackTrace();
        } catch (InvalidKeyException e) {
          
          e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
          
          e.printStackTrace();
        } catch (BadPaddingException e) {
          
          e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
          
          e.printStackTrace();
        }
        
      }
  }

  @Override
  public void close() throws IOException {
    proxySender.close();
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
        
        if(answerSplit.length == 5 && answerSplit[0].equals("!ok") && Arrays.equals(Base64.decode(answerSplit[1].getBytes()), clientChallenge)){
          
          byte[] decodedAES = Base64.decode(answerSplit[3].getBytes());
          AESKey = new SecretKeySpec(decodedAES, 0, decodedAES.length, "AES");
          ivParameter = Base64.decode(answerSplit[4].getBytes());
          this.send(new MessageWrapper(answerSplit[2].getBytes(),false));
          return "!secureChannelCreated";

        } 
      } catch (NoSuchAlgorithmException e) {
        
        e.printStackTrace();
      } catch (NoSuchPaddingException e) {
        
        e.printStackTrace();
      } catch (InvalidKeyException e) {
        
        e.printStackTrace();
      } catch (IllegalBlockSizeException e) {
        
        e.printStackTrace();
      } catch (BadPaddingException e) {
        
        e.printStackTrace();
      } catch (IOException e) {
        
        e.printStackTrace();
      }
    } else if(req instanceof MessageWrapper){
      Cipher cryptCipher;
      try {
        cryptCipher = Cipher.getInstance("AES/CTR/NoPadding");
        SecureRandom secure = new SecureRandom(ivParameter);
        secure.nextBytes(new byte[16]);
        cryptCipher.init(Cipher.DECRYPT_MODE, AESKey, new IvParameterSpec(ivParameter));
        byte[] decryptReq = cryptCipher.doFinal(((MessageWrapper)req).getContent());
        if(((MessageWrapper) req).isMessage()){
          try {
            Object o = Serializer.deserialize(decryptReq);
            if(o instanceof Response){
              return o;
            }
          } catch (ClassNotFoundException e1) {
            
            e1.printStackTrace();
          } catch (IOException e1) {
            
            e1.printStackTrace();
          }
          
        } else {
          
        }
        return new MessageWrapper(decryptReq, false);
      } catch (NoSuchAlgorithmException e) {
        
        e.printStackTrace();
      } catch (NoSuchPaddingException e) {
        
        e.printStackTrace();
      } catch (InvalidKeyException e) {
        
        e.printStackTrace();
      } catch (IllegalBlockSizeException e) {
        
        e.printStackTrace();
      } catch (BadPaddingException e) {
        
        e.printStackTrace();
      } catch (InvalidAlgorithmParameterException e1) {
        
        e1.printStackTrace();
      }
    }
    return new MessageResponse("error occurred");
  }

}

package client;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
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
import message.response.LoginResponse;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.util.encoders.Base64;

import util.Config;


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
        if(parts.length==2){
          
          String pathToPrivateKey = keysDir+"/"+parts[1]+".pem";
          PEMReader pemReader = null;
          try {
            pemReader = new PEMReader(new FileReader(pathToPrivateKey), new PasswordFinder() {

              @Override
              public char[] getPassword() {
              
                /*try {
                  // reads the password from standard input for decrypting the private key
                  //System.out.println("Enter pass phrase:");
                  return new BufferedReader(new InputStreamReader(System.in)).readLine().toCharArray();
                } catch (IOException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                }*/
                return new char[]{'1','2','3','4','5'};//TODO Passwortabfrage
              }
            });
            KeyPair keyPair = (KeyPair) pemReader.readObject(); 
            privateKey = keyPair.getPrivate();
          } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } catch (IOException e) {
            // TODO Auto-generated catch block
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
          
          ByteArrayOutputStream bos = new ByteArrayOutputStream();
          ObjectOutput out = null;
          try {
            out = new ObjectOutputStream(bos);   
            out.writeObject(clientChallenge);
            byte[] challenge = bos.toByteArray();
            byte[] encryptReq = cryptCipher.doFinal(((String)req + " " + new String(Base64.encode(clientChallenge))).getBytes());

            proxySender.send(encryptReq);
          } finally {
            try {
              bos.close();
            } catch (IOException ex) {/* ignore close exception*/}
            try {
              if (out != null) {
                out.close();
              }
            } catch (IOException ex) {/* ignore close exception*/}
          }
        } else {
          cryptCipher = Cipher.getInstance("AES/CTR/NoPadding");
          SecureRandom secure = new SecureRandom(ivParameter);
          secure.nextBytes(new byte[16]);
          cryptCipher.init(Cipher.ENCRYPT_MODE, AESKey, secure);
          byte[] encryptReq = cryptCipher.doFinal(((String)req).getBytes());
          proxySender.send(new MessageWrapper(encryptReq,false));
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
    } else if (req instanceof Request){
      try {
        cryptCipher = Cipher.getInstance("AES/CTR/NoPadding");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;

        out = new ObjectOutputStream(bos);   
        out.writeObject(req);
        byte[] yourBytesReq = bos.toByteArray();
        System.out.println("AES: "+new String(AESKey.getEncoded()));
        System.out.println("IV: "+new String(ivParameter));
        SecureRandom secure = new SecureRandom(ivParameter);
        secure.nextBytes(new byte[16]);
        cryptCipher.init(Cipher.ENCRYPT_MODE, AESKey, secure);
        byte[] encryptReq = cryptCipher.doFinal(yourBytesReq);
        
        System.out.println("xxxxxxxxxxx");
        proxySender.send(new MessageWrapper(encryptReq, true));
          
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
      }else if(req instanceof MessageWrapper){
        System.out.println("3rd message");
        try {
          cryptCipher = Cipher.getInstance("AES/CTR/NoPadding");
          SecureRandom secure = new SecureRandom(ivParameter);
          secure.nextBytes(new byte[16]);
          cryptCipher.init(Cipher.ENCRYPT_MODE, AESKey, secure);
          System.out.println("3rd message content "+new String(Base64.decode(((MessageWrapper) req).getContent())));
          byte[] encryptReq = cryptCipher.doFinal(((MessageWrapper) req).getContent());
          /*cryptCipher.init(Cipher.DECRYPT_MODE, AESKey, new IvParameterSpec(ivParameter), secure);
          byte[] decryptReq = cryptCipher.doFinal(((MessageWrapper) req).getContent());
          System.out.println("3rd message content2 "+new String(Base64.decode(decryptReq)));
*/
          proxySender.send(new MessageWrapper(encryptReq, ((MessageWrapper) req).isMessage()));
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
        System.out.println("gotten challenge: "+new String(Base64.decode(answerSplit[1].getBytes())));
        System.out.println("challenge: "+new String(clientChallenge));
        if(answerSplit.length == 5 && answerSplit[0].equals("!ok") && Arrays.equals(Base64.decode(answerSplit[1].getBytes()), clientChallenge)){
          
          byte[] decodedAES = Base64.decode(answerSplit[3].getBytes());
          AESKey = new SecretKeySpec(decodedAES, 0, decodedAES.length, "AES");
          ivParameter = Base64.decode(answerSplit[4].getBytes());
          System.out.println("proxychallenge: "+new String(Base64.decode(answerSplit[2].getBytes())));
          this.send(new MessageWrapper(answerSplit[2].getBytes(),false));
          return "!secureChannelCreated";

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
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } else if(req instanceof MessageWrapper){
      System.out.println("1"+((MessageWrapper) req).isMessage());
      Cipher cryptCipher;
      try {
        cryptCipher = Cipher.getInstance("AES/CTR/NoPadding");
        SecureRandom secure = new SecureRandom(ivParameter);
        secure.nextBytes(new byte[16]);
        cryptCipher.init(Cipher.DECRYPT_MODE, privateKey, new IvParameterSpec(ivParameter), secure);
        System.out.println("2"+((MessageWrapper) req).isMessage());
        byte[] decryptReq = cryptCipher.doFinal(((MessageWrapper)req).getContent());
        System.out.println("3"+((MessageWrapper) req).isMessage());
        if(((MessageWrapper) req).isMessage()){
          
          ByteArrayInputStream bis = new ByteArrayInputStream(decryptReq);
          ObjectInput in = null;
          try {
            in = new ObjectInputStream(bis);
            Object o = in.readObject(); 
            System.out.println("Send responseobject");
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
        } else {
          
        }
        return new MessageWrapper(decryptReq, false);
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
      } catch (InvalidAlgorithmParameterException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    }
    return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS);
  }

}

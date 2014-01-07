package proxy;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.InputStreamReader;
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
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.util.encoders.Base64;


import util.Config;

import message.MessageWrapper;
import message.Request;
import message.Response;
import message.request.BuyRequest;
import message.request.CreditsRequest;
import message.request.DownloadTicketRequest;
import message.request.ListRequest;
import message.request.LoginRequest;
import message.request.LogoutRequest;
import message.request.UploadRequest;
import message.response.LoginResponse;

public class ClientListenerCrypt implements Runnable, Closeable, Listener{

  private ClientListenerBase64 clientListenerBase64;
  private boolean run;
  private PrivateKey privateKey;
  private PublicKey publicKey;
  private byte[] proxyChallenge;
  private SecretKey AESKey;
  private byte[] ivParameter;
  private boolean connected;
  private ProxyCli proxycli;
  private ProxyImpl proxy;
  
  public ClientListenerCrypt(ClientListenerBase64 clientListenerBase64, ProxyCli proxycli, PrivateKey privateKey){
    this.clientListenerBase64 = clientListenerBase64;
    this.proxycli = proxycli;
    proxy = new ProxyImpl(this.proxycli);
    this.privateKey = privateKey;
  }
  
  @Override
  public void run() {

    run = true;
    while (run) {

      Object o = this.receive();
      if(o != null && o instanceof Request){
        try {
          Response res = sendRequestToProxy((Request) o);
          this.listen(res);
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

    }
  }
  
  @Override
  public void listen(Object req) throws IOException {
    Cipher cryptCipher;
    if (req instanceof Response){
      try {
        cryptCipher = Cipher.getInstance("AES/CTR/NoPadding");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;

        out = new ObjectOutputStream(bos);   
        out.writeObject(req);
        byte[] yourBytesReq = bos.toByteArray();
        
        cryptCipher.init(Cipher.ENCRYPT_MODE, AESKey, new SecureRandom(ivParameter));
        byte[] encryptReq = cryptCipher.doFinal(yourBytesReq);
        clientListenerBase64.listen(new MessageWrapper(encryptReq, true));
      } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      
    } else if(req instanceof String){
      
      try {
        cryptCipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
        cryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        
        byte[] encryptReq = cryptCipher.doFinal(((String)req).getBytes());
        clientListenerBase64.listen(encryptReq);
        
      } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } 
      
    }
  }

  @Override
  public Object receive() {
    Object o = clientListenerBase64.receive();
    System.out.println("crypt object received "+o);
    if(o instanceof byte[]){
      
      Cipher cryptCipher;
      try {
        cryptCipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
        cryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
        
        byte[] decryptReq = cryptCipher.doFinal((byte[])o);
        String answer = new String(decryptReq);
        String[] answerSplit = answer.split(" ");
        
        if(answerSplit.length==3 && answerSplit[0].equals("!login")){
          Config config = new Config("proxy");
          String keysDir = config.getString("keys.dir");
          String pathToPublicKey = keysDir+"/"+answerSplit[1]+".pub.pem";
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
          SecureRandom secureRandom = new SecureRandom(); 
          final byte[] number = new byte[32]; 
          secureRandom.nextBytes(number);
          proxyChallenge = secureRandom.generateSeed(32);
          KeyGenerator generator = KeyGenerator.getInstance("AES"); 
          
          int keysize = 256;
          generator.init(keysize); 
          AESKey = generator.generateKey();
          
          secureRandom.nextBytes(new byte[16]);
         
          byte[] ivparameter = secureRandom.generateSeed(16);
          try {
            
              System.out.println("!ok " + answerSplit[2] + " " + new String(Base64.encode(proxyChallenge))+" "+new String(Base64.encode(AESKey.getEncoded()))+" "+new String(Base64.encode(ivparameter)));
              this.listen("!ok " + answerSplit[2] + " " + new String(Base64.encode(proxyChallenge))+" "+new String(Base64.encode(AESKey.getEncoded()))+" "+new String(Base64.encode(ivparameter)));
            
            
            
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      String res = new String((byte[]) o);
      
    } else if(o instanceof MessageWrapper){
      Cipher cryptCipher;
      try {
        cryptCipher = Cipher.getInstance("AES/CTR/NoPadding");
        cryptCipher.init(Cipher.DECRYPT_MODE, AESKey, new SecureRandom(ivParameter));
        
        byte[] decryptReq = cryptCipher.doFinal(((MessageWrapper) o).getContent());

        if(((MessageWrapper) o).isMessage()){
          if(connected){
            ByteArrayInputStream bis = new ByteArrayInputStream(((MessageWrapper) o).getContent());
            ObjectInput in = null;
            try {
              in = new ObjectInputStream(bis);
              Object req = in.readObject(); 
              return req;
            } catch (IOException | ClassNotFoundException e) {
              e.printStackTrace();
            } finally {
              try {
                bis.close();
              } catch (IOException ex) {}
              try {
                if (in != null) {
                  in.close();
                }
              } catch (IOException ex) {}
            }
            
          }
        } else {

          if(Arrays.equals(this.proxyChallenge, Base64.decode(decryptReq))){
            connected = true;
          }

        }
      } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  @Override
  public void close() throws IOException {
    proxy.close();
    run = false;
  }
  
  public ClientListenerBase64 getListener(){
    return this.clientListenerBase64;
  }
  
  private Response sendRequestToProxy(Request req) throws IOException {
    if (req.getClass().equals(LoginRequest.class)) {
      return proxy.login((LoginRequest) req);
    } else if (req.getClass().equals(BuyRequest.class)) {
      return proxy.buy((BuyRequest) req);
    } else if (req.getClass().equals(CreditsRequest.class)) {
      return proxy.credits();
    } else if (req.getClass().equals(ListRequest.class)) {
      return proxy.list();
    } else if (req.getClass().equals(DownloadTicketRequest.class)) {
      return proxy.download((DownloadTicketRequest) req);
    } else if (req.getClass().equals(UploadRequest.class)) {
      return proxy.upload((UploadRequest) req);
    } else if (req.getClass().equals(LogoutRequest.class)) {
      return proxy.logout();
    }
    return null;
  }

}

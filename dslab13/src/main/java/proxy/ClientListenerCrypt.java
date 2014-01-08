package proxy;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
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
import javax.crypto.spec.IvParameterSpec;

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

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.util.encoders.Base64;

import util.Config;
import util.Serializer;

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
  private String user;
  
  
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
      
      if(o==null){
        if(user!=null){
          System.out.println("Client "+user+" disconnected");
        }
        
        try {
          proxy.logout();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        run=false;
      }
      if(o != null && o instanceof Request){
        try {
          Response res = sendRequestToProxy((Request) o);
          this.listen(res);
        } catch (IOException e) {

        }
      }

    }
  }
  
  @Override
  public void listen(Object req) throws IOException {
    Cipher cryptCipher;
    if (req instanceof Response){
      try{
        cryptCipher = Cipher.getInstance("AES/CTR/NoPadding");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;

        out = new ObjectOutputStream(bos);   
        out.writeObject(req);
        byte[] yourBytesReq = bos.toByteArray();
        
        cryptCipher.init(Cipher.ENCRYPT_MODE, AESKey, new IvParameterSpec(ivParameter));
        byte[] encryptReq = cryptCipher.doFinal(yourBytesReq);
        clientListenerBase64.listen(new MessageWrapper(encryptReq, true));
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
      
      
    } else if(req instanceof String){
      
      try {
        final String B64 = "a-zA-Z0-9/+";
        assert ((String)req).matches("!ok ["+B64+"]{43}= ["+B64+"]{43}= ["+B64+"]{43}= ["+B64+"]{22}==") : "2nd message";
        
        cryptCipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
        cryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        
        byte[] encryptReq = cryptCipher.doFinal(((String)req).getBytes());
        clientListenerBase64.listen(encryptReq);
        
      }  catch (NoSuchAlgorithmException e) {
        
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
      
    }
  }

  @Override
  public Object receive() {
    Object o = clientListenerBase64.receive();
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
          this.user = answerSplit[1];
          String pathToPublicKey = keysDir+"/"+user+".pub.pem";
          
          PEMReader pemReader;
          try {
            pemReader = new PEMReader(new FileReader(pathToPublicKey));
            publicKey = (PublicKey) pemReader.readObject();
          } catch (FileNotFoundException e) {
            
            e.printStackTrace();
          } catch (IOException e) {
            
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
         
          ivParameter = secureRandom.generateSeed(16);
          try {

            this.listen("!ok " + answerSplit[2] + " " + new String(Base64.encode(proxyChallenge))+" "+new String(Base64.encode(AESKey.getEncoded()))+" "+new String(Base64.encode(ivParameter)));
            
          } catch (IOException e) {
            
            e.printStackTrace();
          }
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
      String res = new String((byte[]) o);
      return res;
    } else if(o instanceof MessageWrapper){
      Cipher cryptCipher;
      try {
        
        cryptCipher = Cipher.getInstance("AES/CTR/NoPadding");
        SecureRandom secure = new SecureRandom(ivParameter);
        secure.nextBytes(new byte[16]);
        cryptCipher.init(Cipher.DECRYPT_MODE, AESKey, new IvParameterSpec(ivParameter));
        
        byte[] decryptReq = (cryptCipher.doFinal(((MessageWrapper) o).getContent()));
        
        if(((MessageWrapper) o).isMessage()){
          if(connected){
            try {
              return Serializer.deserialize(decryptReq);
            } catch (ClassNotFoundException e) {
              
              e.printStackTrace();
            } catch (IOException e) {
              
              e.printStackTrace();
            }
          }
        } else {
          byte[] decodedReq = Base64.decode(decryptReq);
          if(Arrays.equals(this.proxyChallenge, decodedReq)){
            connected = true;
            return new String("AESChannel completed!");
          }

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
      } catch (InvalidAlgorithmParameterException e1) {
        
        e1.printStackTrace();
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

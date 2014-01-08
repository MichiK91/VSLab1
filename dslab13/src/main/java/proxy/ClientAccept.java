package proxy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;

import util.Config;

public class ClientAccept implements Runnable{
	
	private ExecutorService threads;
	private ServerSocket s;
	private ProxyCli proxycli;
	private ClientListenerCrypt cl;
	
	private ArrayList<ClientListenerCrypt> list;
	
	private PrivateKey privateKey;
	
	public ClientAccept(Config config, ProxyCli proxycli){
		this.proxycli = proxycli;
		this.list = new ArrayList<ClientListenerCrypt>();
		try {
			this.s = new ServerSocket(config.getInt("tcp.port"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		threads = Executors.newCachedThreadPool();
	}
	@Override
	public void run() {
	  readPrivateKey();
		while(true){
			try {
			  System.out.println("beginrunproxy");
				cl = new ClientListenerCrypt(new ClientListenerBase64(new ClientListenerTCP()),proxycli, privateKey);
				cl.getListener().getListener().setSocket(s.accept());
				System.out.println("acceptedrunproxy");
				list.add(cl);
				threads.execute(cl);
			} catch (Exception e) {
				//socket closed - no clients till now
				System.out.println("Proxy shuts down. No further clients are allowed.");
				break;
			}
		}
		
	}
	
	public void close() throws IOException{
		for(ClientListenerCrypt c : list){
			if(c != null)
				c.getListener().getListener().close();
		}
		threads.shutdownNow();
		s.close();
	}
	
	private void readPrivateKey(){
	  Config config = new Config("proxy");
    String pathToPrivateKey = config.getString("key");
    
    PEMReader in;
    try {
      in = new PEMReader(new FileReader(pathToPrivateKey), new PasswordFinder() {
        @Override
        public char[] getPassword() {

          return "12345".toCharArray();
          
        }
        
      });
      KeyPair keyPair = (KeyPair) in.readObject(); 
      System.out.println("keypair read");
      privateKey = keyPair.getPrivate();
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

	}

}

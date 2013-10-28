package proxy;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import util.Config;
import your.Proxy;

public class ClientAccept {
	
	private ExecutorService threads;
	private ServerSocket s;
	private Config config;
	private ProxyCli proxycli;
	
	public ClientAccept(Config config, ProxyCli proxycl){
		this.config = config;
		this.proxycli = proxycli;
		this.s = s;
		threads = Executors.newCachedThreadPool();
		run();
	}
	public void run() {
		 
		while(true){
			try {
				ClientListenerTCP cl = new ClientListenerTCP(config,proxycli);
				threads.execute(cl);
			} catch (Exception e) {
				threads.shutdown();
				e.printStackTrace();
			}
		}
		
	}

}

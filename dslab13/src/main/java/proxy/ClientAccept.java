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
	
	public ClientAccept(Config config, ProxyCli proxycli){
		this.config = config;
		this.proxycli = proxycli;
		try {
			this.s = new ServerSocket(config.getInt("tcp.port"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		threads = Executors.newCachedThreadPool();
		run();
	}
	public void run() {
		 
		while(true){
			try {
				ClientListenerTCP cl = new ClientListenerTCP(s.accept(),proxycli);
				threads.execute(cl);
			} catch (Exception e) {
				threads.shutdown();
				e.printStackTrace();
			}
		}
		
	}

}

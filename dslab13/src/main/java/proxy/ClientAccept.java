package proxy;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import util.Config;
import your.Proxy;

public class ClientAccept implements Runnable, Closeable {
	
	private ExecutorService threads;
	private ServerSocket s;
	private Config config;
	
	public ClientAccept(Config config){
		this.config = config;
		try {
			this.s = new ServerSocket(config.getInt("tcp.port"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		threads = Executors.newCachedThreadPool();
	}
	@Override
	public void run() {
		 
//		while(true){
//			try {
//				threads.execute(new ClientListener(s.accept(),config));
//			} catch (SocketException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
		
	}
	@Override
	public void close() throws IOException {
		s.close();
		threads.shutdown();
	}

}

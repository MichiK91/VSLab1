package proxy;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import util.Config;
import your.Proxy;

public class ClientAccept implements Runnable{
	
	private ExecutorService threads;
	private ServerSocket s;
	private ProxyCli proxycli;
	private ClientListenerTCP cl;
	
	public ClientAccept(Config config, ProxyCli proxycli){
		this.proxycli = proxycli;
		try {
			this.s = new ServerSocket(config.getInt("tcp.port"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		threads = Executors.newCachedThreadPool();
		//run();
	}
	@Override
	public void run() {
		 
		while(true){
			try {
				cl = new ClientListenerTCP(s.accept(),proxycli);
				threads.execute(cl);
			} catch (Exception e) {
				//socket closed
				break;
			}
		}
		
	}
	
	public void close() throws IOException{
		if(cl != null)
			cl.close();
		threads.shutdown();
		s.close();
	}

}

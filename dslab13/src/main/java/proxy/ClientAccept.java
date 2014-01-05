package proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import util.Config;

public class ClientAccept implements Runnable{
	
	private ExecutorService threads;
	private ServerSocket s;
	private ProxyCli proxycli;
	private ClientListenerCrypt cl;
	
	private ArrayList<ClientListenerCrypt> list;
	
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
		 
		while(true){
			try {
				cl = new ClientListenerCrypt(new ClientListenerBase64(new ClientListenerTCP(s.accept(),proxycli)));
				list.add(cl);
				threads.execute(cl.getListener().getListener());
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

}

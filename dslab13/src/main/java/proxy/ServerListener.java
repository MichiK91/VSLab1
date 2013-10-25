package proxy;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.sql.Timestamp;

import model.FileServerInfo;

import util.Config;

public class ServerListener implements Runnable, Closeable {

	private Config config;
	private DatagramSocket socket;
	private byte[] b;
	private DatagramPacket packet;
	private boolean run;
	private TimerTask tt;
	private Timer timer;

	private long sek = 0;
	
	private ArrayList<FileServerInfo> files;
	//private HashMap<FileServerInfo, Long> hm;

	public ServerListener(Config config) throws SocketException {
		this.socket = new DatagramSocket(config.getInt("udp.port"));
		this.b = new byte[256];
		this.packet = new DatagramPacket(this.b, this.b.length);
		this.config = config;
		this.run = true;
		this.files = new ArrayList<FileServerInfo>();
		//this.hm = new HashMap<FileServerInfo, Long>();
	}

	@Override
	public void run() {
		//TODO: 3 sekunden timeout
		tt = new TimerTask(){
			int count = 0;
			@Override
			public void run() {
				try {
					socket.receive(packet);
					String s = new String(packet.getData());
					String port = s.substring(s.lastIndexOf(" ")+1, s.lastIndexOf(" ")+6);
					InetAddress addr = packet.getAddress();
					
					boolean file = true;
					for(FileServerInfo f : files){
						if(addr.equals(f.getAddress()) && port.equals(""+f.getPort())){
							file = false;
						}
					}
					
					if(file){
						files.add(new FileServerInfo(addr, Integer.parseInt(port), 0, true));
						sek = System.currentTimeMillis();
						//hm.put(new FileServerInfo(addr, Integer.parseInt(port), 0, true), System.currentTimeMillis());
					} 
					else {
						for(FileServerInfo f : files){
							if(addr.equals(f.getAddress()) && port.equals(""+f.getPort())){
								//System.out.println(System.currentTimeMillis() - sek);
								sek = System.currentTimeMillis();

							}
						}
//						for(Entry<FileServerInfo,Long> e: hm.entrySet()){
//							if(e.getKey().getAddress().equals(addr) && port.equals(""+e.getKey().getPort())){
//								if(System.currentTimeMillis()-e.getValue() > 3000){
//									hm.remove(e);
//									files.remove(e.getKey());
//									files.add(new FileServerInfo(addr, Integer.parseInt(port), 0, false));
//									hm.put(new FileServerInfo(addr, Integer.parseInt(port), 0, false), System.currentTimeMillis());
//								} else{
//									hm.remove(e);
//									hm.put(new FileServerInfo(addr, Integer.parseInt(port), 0, true), System.currentTimeMillis());
//								}
//								
//							}
//						}
					}
					
					

				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			
		};
		timer = new Timer();
		timer.schedule(tt, 0, config.getInt("fileserver.checkPeriod"));
			
		

	}

	@Override
	public void close() throws IOException {
		timer.cancel();
		tt.cancel();
		socket.close();
		run = false;

	}
	
	public ArrayList<FileServerInfo> getServers(){
		return files;
	}

}

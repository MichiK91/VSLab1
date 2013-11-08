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

public class ServerListenerUDP implements Runnable, Closeable {

	private Config config;
	private DatagramSocket socket;
	private byte[] b;
	private DatagramPacket packet;
	private TimerTask tt;
	private Timer timer;

	private FileServerInfo files;

	private  HashMap<FileServerInfo, Long> hm;
	private ArrayList<FileServerInfo> fileserver;

	public ServerListenerUDP(Config config) throws SocketException {
		this.socket = new DatagramSocket(config.getInt("udp.port"));
		this.b = new byte[256];
		this.packet = new DatagramPacket(this.b, this.b.length);
		this.config = config;
		this.hm = new HashMap<FileServerInfo, Long>();
		this.fileserver = new ArrayList<FileServerInfo>();

		check();
	}

	private void check() {

		tt = new TimerTask() {

			@Override
			public void run() {
				for (Entry<FileServerInfo, Long> e : hm.entrySet()) {
					if (System.currentTimeMillis() - e.getValue() > 3000) {
						hm.remove(e.getKey());
						setOffline(e.getKey());
					}
				}
			}
		};
		timer = new Timer();
		timer.schedule(tt, 0, config.getInt("fileserver.checkPeriod"));
	}

	@Override
	public void run() {
		while (true) {

			try {
				socket.receive(packet);
				String s = new String(packet.getData());
				String port = s.substring(s.lastIndexOf(" ") + 1,
						s.lastIndexOf(" ") + 6);
				InetAddress addr = packet.getAddress();

				boolean found = false;
				for (Entry<FileServerInfo, Long> e : hm.entrySet()) {
					if (addr.equals(e.getKey().getAddress())
							&& port.equals("" + e.getKey().getPort())) {
						hm.put(e.getKey(), System.currentTimeMillis());
						found = true;
					}
				}
				if (!found) {
					System.out.println("new server");
					files = new FileServerInfo(addr, Integer.parseInt(port), 0,
							true);
					hm.put(files, System.currentTimeMillis());
					if (checkOffline(files)) {
						setOnline(files);
					} else {
						fileserver.add(files);
					}
				}

			} catch (IOException e) {
				//socket closed
				break;
			}
		}

	}

	@Override
	public void close() throws IOException {

		socket.close();
		timer.cancel();
		tt.cancel();

	}

	// checks, if a server was already online
	public boolean checkOffline(FileServerInfo file) {
		for (FileServerInfo f : fileserver) {
			if (f.getAddress().equals(file.getAddress())
					&& file.getPort() == f.getPort()) {
				return true;
			}
		}
		return false;
	}

	// sets the serverstatus offline
	public void setOffline(FileServerInfo file) {
		for (FileServerInfo f : fileserver) {
			if (f.getAddress().equals(file.getAddress())
					&& file.getPort() == f.getPort()) {
				fileserver.remove(f);
				fileserver.add(new FileServerInfo(f.getAddress(), f.getPort(),
						f.getUsage(), false));
			}
		}
	}

	// sets the serverstatus online
	public void setOnline(FileServerInfo file) {
		for (FileServerInfo f : fileserver) {
			if (f.getAddress().equals(file.getAddress())
					&& file.getPort() == f.getPort()) {
				fileserver.remove(f);
				fileserver.add(new FileServerInfo(f.getAddress(), f.getPort(),
						f.getUsage(), true));
			}
		}
	}

	// returns the list of servers
	public ArrayList<FileServerInfo> getServers() {
		return fileserver;
	}

}

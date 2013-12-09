package proxy;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import model.FileServerInfo;

import util.Config;

public class ServerListenerUDP implements Runnable, Closeable {

	private Config config;
	private DatagramSocket socket;
	private byte[] b;
	private DatagramPacket packet;
	private TimerTask tt;
	private Timer timer;

	private List<FileServerTime> server;

	public ServerListenerUDP(Config config) throws SocketException {
		this.socket = new DatagramSocket(config.getInt("udp.port"));
		this.b = new byte[256];
		this.packet = new DatagramPacket(this.b, this.b.length);
		this.config = config;
		this.server = Collections
				.synchronizedList(new ArrayList<FileServerTime>());

		check();
	}

	private void check() {

		tt = new TimerTask() {

			@Override
			public void run() {
				synchronized (server) {
					for (FileServerTime f : server) {
						if (System.currentTimeMillis() - f.getTimestamp() > 3000) {
							f.setOnline(false);
						}
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
				synchronized (server) {
					for (FileServerTime f : server) {
						if (f.equalsImportant(addr, Integer.parseInt(port))) {
							found = true;
							f.setOnline(true);
							f.setTimestamp(System.currentTimeMillis());
						}
					}
				}
				if (!found) {
					server.add(new FileServerTime(addr, Integer.parseInt(port),
							0, true, System.currentTimeMillis()));
				}
			} catch (IOException e) {
				// socket closed
				System.out
						.println("Proxy shuts down. No further servers are allowed.");
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

	// changes the usage count
	public void changeServer(FileServerInfo f) {
		synchronized (server) {
			for (FileServerTime e : server) {
				if (e.equalsImportant(f.getAddress(), f.getPort())) {
					e.setUsage(f.getUsage());
				}
			}
		}
	}

	// returns list of all current server
	public List<FileServerInfo> getServers() {
		List<FileServerInfo> li = new ArrayList<FileServerInfo>();
		synchronized (server) {
			for (FileServerTime f : server) {
				li.add(f.getFileServerInfo());
			}
		}
		return li;
	}

}

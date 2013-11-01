package server;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import util.Config;
import convert.*;

public class ProxySenderUDP implements Closeable {

	private DatagramSocket socket;
	private DatagramPacket packet;
	private byte[] b;
	private Config config;
	private String info;

	private boolean run;

	private TimerTask tt;
	private Timer timer;
	

	public ProxySenderUDP(Config config) {
		this.config = config;
		this.info = "!alive " + this.config.getInt("tcp.port");
		try {
			this.socket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.b = new byte[256];
		run();
	}

	public void run() {
		run = true;
		tt = new TimerTask() {

			@Override
			public void run() {

				try {
					b = info.getBytes();
					packet = new DatagramPacket(b, b.length,
							InetAddress.getByName(config
									.getString("proxy.host")),
							config.getInt("proxy.udp.port"));
					socket.send(packet);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

		};

		timer = new Timer();
		timer.schedule(tt, 0, config.getInt("fileserver.alive"));

	}

	@Override
	public void close() throws IOException {
		run = false;
		tt.cancel();
		timer.cancel();
		socket.close();
	}
	

}

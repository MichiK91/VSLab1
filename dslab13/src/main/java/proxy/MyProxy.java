package proxy;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.Properties;

import util.Config;

public class MyProxy {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Config c = new Config("proxy");
		try {
			
			int i = c.getInt("tcp.port");
			System.out.println(i);
			
			/*ServerSocket server = new ServerSocket(9000);
			Socket socket = server.accept();
			BufferedReader in = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			String str = in.readLine();
			System.out.println(str);
			socket.close();
			server.close();*/
			
		} catch (Exception e) {

		}

	}
}

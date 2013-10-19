package proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MyProxy {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ServerSocket server = new ServerSocket(9000);
			// warte solange bis ein client eine verbindung herstellt
			Socket socket = server.accept();
			BufferedReader in = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			String str = in.readLine();
			System.out.println(str);
			socket.close();
			server.close();
		} catch (Exception e) {

		}

	}
}

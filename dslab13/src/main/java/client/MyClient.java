package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class MyClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Socket s = new Socket("localhost", 9000);
			PrintStream os = new PrintStream(s.getOutputStream());
			os.println("Hallo Server");
			s.close();
		} catch (Exception e) {
			System.out.println("Error");
		}
	}

}

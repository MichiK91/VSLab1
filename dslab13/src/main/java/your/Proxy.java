package your;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.Properties;

import cli.Shell;

import util.ComponentFactory;
import util.Config;

public class Proxy {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Config config = new Config("proxy");
		Shell shell = new Shell("proxy", new DataOutputStream(System.out), new DataInputStream(System.in));
		
		ComponentFactory fact = new ComponentFactory();
		
		try {
			fact.startProxy(config, shell);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/*try {
			
			int i = c.getInt("tcp.port");
			System.out.println(i);
			
			ServerSocket server = new ServerSocket(9000);
			Socket socket = server.accept();
			BufferedReader in = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			String str = in.readLine();
			System.out.println(str);
			socket.close();
			server.close();
			
		} catch (Exception e) {

		}*/

	}
}

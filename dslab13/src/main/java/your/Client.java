package your;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import util.ComponentFactory;
import util.Config;
import cli.Shell;

public class Client {

	public static void main(String[] args) {
		Config config = new Config("client");
		Shell shell = new Shell("client", new DataOutputStream(System.out), new DataInputStream(System.in));
		
		ComponentFactory fact = new ComponentFactory();
		
		try {
			fact.startClient(config, shell);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/*try {
			Socket s = new Socket("localhost", 9000);
			PrintStream os = new PrintStream(s.getOutputStream());
			os.println("Hallo Server");
			s.close();
		} catch (Exception e) {
			System.out.println("Error");
		}*/
	}

}

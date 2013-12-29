package your;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import util.ComponentFactory;
import util.Config;
import cli.Shell;
import client.ClientRMI;

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
	}

}

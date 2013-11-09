package your;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import cli.Shell;

import util.ComponentFactory;
import util.Config;

public class Proxy {

	
	public static void main(String[] args) {
		Config config = new Config("proxy");
		Shell shell = new Shell("proxy", new DataOutputStream(System.out), new DataInputStream(System.in));
		
		ComponentFactory fact = new ComponentFactory();
		
		try {
			fact.startProxy(config, shell);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

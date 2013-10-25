package your;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import util.ComponentFactory;
import util.Config;
import cli.Shell;

public class Fileserver {

	
	public static void main(String[] args) {
		Config config = new Config("fs"+args[0]);
		
		Shell shell = new Shell("fileserver", new DataOutputStream(System.out), new DataInputStream(System.in));
		
		ComponentFactory fact = new ComponentFactory();
		
		try {
			fact.startFileServer(config, shell);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

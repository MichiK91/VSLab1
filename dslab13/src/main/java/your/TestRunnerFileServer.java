package your;

import server.FileServerCli;
import util.CliComponent;

public class TestRunnerFileServer implements Runnable {

	private FileServerCli fs;
	private CliComponent component;

	public TestRunnerFileServer(FileServerCli fs, CliComponent component) {
		this.fs = fs;
		this.component = component;
	}

	@Override
	public void run() {
		System.out.println("fs started");

	}

	public void exit() {
		component.getIn().addLine("!exit");
	}

}

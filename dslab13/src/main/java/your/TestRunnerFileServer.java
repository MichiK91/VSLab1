package your;

import server.FileServerCli;

public class TestRunnerFileServer implements Runnable {

	private FileServerCli fs;

	public TestRunnerFileServer(FileServerCli fs) {
		this.fs = fs;
	}

	@Override
	public void run() {
		System.out.println("fs");

	}

}

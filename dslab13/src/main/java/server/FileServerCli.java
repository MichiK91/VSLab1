package server;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import message.response.MessageResponse;
import util.Config;
import cli.Command;
import cli.Shell;

public class FileServerCli implements IFileServerCli {
	private Shell shell;

	// thread pool
	private ExecutorService threads = Executors.newCachedThreadPool();

	private FileServerImpl fileserver;

	private TCPHandler handler;

	public FileServerCli(Config config, Shell shell) throws SocketException {
		this.shell = shell;

		// register the shell
		this.shell.register(this);
		this.threads.execute(this.shell);

		fileserver = new FileServerImpl(config);

		this.handler = new TCPHandler(config);
		this.threads.execute(this.handler);

	}

	@Override
	@Command
	public MessageResponse exit() throws IOException {
		fileserver.close();
		handler.close();
		threads.shutdownNow();
		shell.close();
		return null;
	}

}

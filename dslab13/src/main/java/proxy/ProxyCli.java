package proxy;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import util.Config;

import message.Response;
import message.response.FileServerInfoResponse;
import message.response.MessageResponse;
import message.response.UserInfoResponse;
import model.FileServerInfo;
import model.UserInfo;
import cli.Command;
import cli.Shell;

public class ProxyCli implements IProxyCli {
	
	private Shell shell;
	
	//thread pool
	private ExecutorService threads = Executors.newCachedThreadPool();
	
	private ProxyImpl proxy;
		
	public ProxyCli(Config config,Shell shell) throws SocketException{
		this.shell = shell;
		
		//register the shell
		this.shell.register(this);
		this.threads.execute(this.shell);
		
		proxy = new ProxyImpl(config);
	}

	@Override
	@Command
	public Response fileservers() throws IOException {
		return proxy.fileservers();
	}

	@Override
	@Command
	public Response users() throws IOException {
		return proxy.users();
	}

	@Override
	@Command
	public MessageResponse exit() throws IOException {
		//FEHLER
		threads.shutdown();
		shell.close();
		proxy.close();
		return null;
	}

}

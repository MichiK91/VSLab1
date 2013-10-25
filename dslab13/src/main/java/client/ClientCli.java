package client;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import proxy.ServerListener;
import util.Config;
import cli.Command;
import cli.Shell;

import message.Response;
import message.request.*;
import message.response.*;
import model.FileServerInfo;
import model.UserInfo;

public class ClientCli implements IClientCli {

	private Config config;
	private Shell shell;

	// thread pool
	private ExecutorService threads = Executors.newCachedThreadPool();
	private ProxySenderTCP sender;

	public ClientCli(Config config, Shell shell) throws SocketException {
		this.config = config;
		this.shell = shell;

		// register the shell
		this.shell.register(this);
		this.threads.execute(this.shell);

		sender = new ProxySenderTCP(config);

	}

	@Override
	@Command
	public LoginResponse login(String username, String password)
			throws IOException {
		LoginRequest lreq = new LoginRequest(username,password);
		LoginResponse lres = (LoginResponse) sender.send(lreq);
		return lres;
	}

	@Override
	@Command
	public Response credits() throws IOException {
		CreditsRequest creq = new CreditsRequest();
		Response res = sender.send(creq);
		return res;
	}

	@Override
	@Command
	public Response buy(long credits) throws IOException {
		BuyRequest breq = new BuyRequest(credits);
		Response res = sender.send(breq);
		return res;
	}

	@Override
	@Command
	public Response list() throws IOException {
		ListRequest lreq = new ListRequest();
		Response res = sender.send(lreq);
		return res;
	}

	@Override
	@Command
	public Response download(String filename) throws IOException {
		DownloadTicketRequest dreq = new DownloadTicketRequest(filename);
		Response res = sender.send(dreq);
		
		
		//TODO: download process
		return null;
	}

	@Override
	@Command
	public MessageResponse upload(String filename) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public MessageResponse logout() throws IOException {
		LogoutRequest lreq = new LogoutRequest();
		MessageResponse mres = (MessageResponse) sender.send(lreq);
		return mres;
	}

	@Override
	@Command
	public MessageResponse exit() throws IOException {
		sender.send(new LogoutRequest());
		threads.shutdown();
		shell.close();
		sender.close();
		return null;
	}

}

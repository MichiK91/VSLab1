package client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import proxy.ServerListenerUDP;
import util.Config;
import cli.Command;
import cli.Shell;

import message.Response;
import message.request.*;
import message.response.*;
import model.DownloadTicket;
import model.FileServerInfo;
import model.UserInfo;

public class ClientCli implements IClientCli {

	private Config config;
	private Shell shell;

	// thread pool
	private ExecutorService threads = Executors.newCachedThreadPool();
	private ProxySenderTCP psender;	
	private ServerSenderTCP ssender;

	public ClientCli(Config config, Shell shell) throws SocketException {
		this.config = config;
		this.shell = shell;

		// register the shell
		this.shell.register(this);
		this.threads.execute(this.shell);

		psender = new ProxySenderTCP(config);

	}

	@Override
	@Command
	public LoginResponse login(String username, String password)
			throws IOException {
		LoginRequest lreq = new LoginRequest(username,password);
		LoginResponse lres = (LoginResponse) psender.send(lreq);
		return lres;
	}

	@Override
	@Command
	public Response credits() throws IOException {
		CreditsRequest creq = new CreditsRequest();
		Response res = psender.send(creq);
		return res;
	}

	@Override
	@Command
	public Response buy(long credits) throws IOException {
		BuyRequest breq = new BuyRequest(credits);
		Response res = psender.send(breq);
		return res;
	}

	@Override
	@Command
	public Response list() throws IOException {
		ListRequest lreq = new ListRequest();
		Response res = psender.send(lreq);
		return res;
	}

	@Override
	@Command
	public Response download(String filename) throws IOException {
		DownloadTicketRequest dtreq = new DownloadTicketRequest(filename);
		Object res = psender.send(dtreq);
		DownloadTicketResponse dtres;
		if(res instanceof DownloadTicketResponse){
			dtres = (DownloadTicketResponse) res;
		} else{
			return (Response) res;
		}
		//TODO: download 2mal - kommt ins else!
		
		
		//TODO: download process
		//verbindung zum Server
		DownloadTicket dt = dtres.getTicket();
		DownloadFileRequest dfreq = new DownloadFileRequest(dt);
		ssender = new ServerSenderTCP(dt.getAddress(), dt.getPort());
		DownloadFileResponse dfres = (DownloadFileResponse) ssender.send(dfreq);
		
		//create file
		String dir = config.getString("download.dir");
		
		//first delete existing file
		File file = new File(dir);
		File[] files = file.listFiles();

		for (File f : files) {
			if (f.isFile()) {
				if(f.getName().equals(dfres.getTicket().getFilename())){
					f.delete();
				}
			}
		}
		FileOutputStream out = new FileOutputStream(dir+"/"+dfres.getTicket().getFilename());
        out.write(dfres.getContent());
        out.close(); 
        
		return dfres;
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
		MessageResponse mres = (MessageResponse) psender.send(lreq);
		return mres;
	}

	@Override
	@Command
	public MessageResponse exit() throws IOException {
		psender.send(new LogoutRequest());
		threads.shutdown();
		shell.close();
		psender.close();
		return null;
	}

}

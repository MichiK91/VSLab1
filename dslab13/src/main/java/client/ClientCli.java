package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import util.Config;
import cli.Command;
import cli.Shell;

import message.Response;
import message.request.*;
import message.response.*;
import model.DownloadTicket;

public class ClientCli implements IClientCli {

	private Config config;
	private Shell shell;

	// thread pool
	private ExecutorService threads = Executors.newCachedThreadPool();
	private ProxySenderTCP psender;	
	private ServerSenderTCP ssender;
	
	private boolean login;
	
	private ClientRMI rmi;
	
	private String username;

	public ClientCli(Config config, Shell shell) throws IOException {
		this.config = config;
		this.shell = shell;
		this.login = false;
		this.username = "";

		// register the shell
		this.shell.register(this);
		this.threads.execute(this.shell);

		try {
			psender = new ProxySenderTCP(config);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("No proxy available. Please press enter to exit.");			
			exit();
		}
		
		//start RMI
		rmi = new ClientRMI(this);
		this.shell.register(rmi);

	}

	@Override
	@Command
	public LoginResponse login(String username, String password)
			throws IOException {
		LoginRequest lreq = new LoginRequest(username,password);
		LoginResponse lres = (LoginResponse) psender.send(lreq);
		if(lres.getType().equals(LoginResponse.Type.SUCCESS)){
			this.username = username;
			login = true;
		}
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
		
		
		//connection to server
		DownloadTicket dt = dtres.getTicket();
		DownloadFileRequest dfreq = new DownloadFileRequest(dt);
		ssender = new ServerSenderTCP(dt.getAddress(), dt.getPort());
		
		Object res1 = ssender.send(dfreq);
		DownloadFileResponse dfres;
		if(res1 instanceof DownloadFileResponse){
			dfres = (DownloadFileResponse) res1;
		} else{
			return (Response) res1;
		}
		
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
		
		String dir = config.getString("download.dir");
		
		File file = new File(dir);
		File[] files = file.listFiles();
		byte[] content = null;
		
		boolean foundfile = false;
		
		for (File f : files) {
			if (f.isFile()) {
				if(f.getName().equals(filename)){
					foundfile = true;
					//read content
					FileInputStream in = new FileInputStream(f);
					String s = "";
					while(true){
						int read = in.read();
						if(read == -1){
							break;
						} else{
							char c = (char) read;
				            s += c; 
						}
					}
					content = s.getBytes();
					in.close();
				}
			}
		}
		if(!foundfile)
			return new MessageResponse("Invalid file name");
		UploadRequest ureq = new UploadRequest(filename, 1, content);
		MessageResponse res = (MessageResponse) psender.send(ureq);
		return res;
	}

	@Override
	@Command
	public MessageResponse logout() throws IOException {
		LogoutRequest lreq = new LogoutRequest();
		MessageResponse mres = (MessageResponse) psender.send(lreq);
		login = false;
		this.username = "";
		return mres;
	}

	@Override
	@Command
	public MessageResponse exit() throws IOException {
		if(rmi != null)
			rmi.close();
		if(login)
			logout();
		if(psender != null)
			psender.close();
		threads.shutdownNow();
		shell.close();
		return null;
	}
	
	public boolean isLogin() {
		return this.login;
	}
	
	public String getUsername(){
		return this.username;
	}
	
	public Set<String> getListOfFiles() throws IOException {
		ListRequest lreq = new ListRequest();
		ListResponse res = (ListResponse) psender.send(lreq);
		return res.getFileNames();
	}
	
	public void notify(String filename, long numberOfDownloads){
		try {
			shell.writeLine("Notification: " + filename + " got downloaded " + numberOfDownloads + " times!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}

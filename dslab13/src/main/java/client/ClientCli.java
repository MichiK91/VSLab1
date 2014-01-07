package client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bouncycastle.util.encoders.Base64;

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
	private ProxySenderCrypt psender;	
	private ServerSenderTCP ssender;
	
	private boolean login;

	public ClientCli(Config config, Shell shell) throws IOException {
		this.config = config;
		this.shell = shell;
		this.login = false;

		// register the shell
		this.shell.register(this);
		this.threads.execute(this.shell);
		System.out.println("12345");
		try {
		  System.out.println("12345");
			psender = new ProxySenderCrypt(new ProxySenderBase64(new ProxySenderTCP(config)));
			System.out.println("12345");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("No proxy available. Please press enter to exit.");			
			exit();
		}

	}

	@Override
	@Command
	public LoginResponse login(String username, String password)
			throws IOException {
      psender.send("!login " + username);
      Object res = psender.receive();
      if(res instanceof String && res.equals("!secureChannelCreated")){
        psender.send(new LoginRequest(username, password));
        LoginResponse lres = (LoginResponse)psender.receive();
        if(((LoginResponse) lres).getType().equals(LoginResponse.Type.SUCCESS)){
          login = true;
        }
        return (LoginResponse) lres;
      }
      return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS);
           
          
	}

	@Override
	@Command
	public Response credits() throws IOException {
		CreditsRequest creq = new CreditsRequest();
		psender.send(creq);
		Response res = (Response) psender.receive();
		return res;
	}

	@Override
	@Command
	public Response buy(long credits) throws IOException {
		BuyRequest breq = new BuyRequest(credits);
		psender.send(breq);
    Response res = (Response) psender.receive();
		return res;
	}

	@Override
	@Command
	public Response list() throws IOException {
		ListRequest lreq = new ListRequest();
		psender.send(lreq);
    Response res = (Response) psender.receive();
		return res;
	}

	@Override
	@Command
	public Response download(String filename) throws IOException {
		DownloadTicketRequest dtreq = new DownloadTicketRequest(filename);
		psender.send(dtreq);
    Response res = (Response) psender.receive();
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
		
		psender.send(dfreq);
    Response res1 = (Response) ssender.receive();

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
		psender.send(ureq);
		MessageResponse res = (MessageResponse) psender.receive();
		return res;
	}

	@Override
	@Command
	public MessageResponse logout() throws IOException {
		LogoutRequest lreq = new LogoutRequest();
		
		psender.send(lreq);
		MessageResponse mres = (MessageResponse) psender.receive();
		login = false;
		return mres;
	}

	@Override
	@Command
	public MessageResponse exit() throws IOException {
		if(login)
			logout();
		if(psender != null)
			psender.close();
		threads.shutdownNow();
		shell.close();
		return null;
	}

}

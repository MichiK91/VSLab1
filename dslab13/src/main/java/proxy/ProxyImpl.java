package proxy;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import util.ChecksumUtils;
import util.Config;

import message.Response;
import message.request.*;
import message.response.*;
import model.DownloadTicket;
import model.FileServerInfo;
import model.UserInfo;

public class ProxyImpl implements IProxy, Closeable {

	private Config config;
	private Config userconfig;

	// thread pool
	private ExecutorService threads = Executors.newCachedThreadPool();

	private ServerListener s_listener;

	private ClientListener c_listener;

	private String user;
	private long creditscount;

	private boolean loggedin;

	private ArrayList<UserInfo> users = new ArrayList<UserInfo>();

	public ProxyImpl(Config config) throws SocketException {
		this.config = config;
		this.userconfig = new Config("user");

		// thread pool for the servers
		this.s_listener = new ServerListener(this.config);
		this.threads.execute(this.s_listener);
		// thread pool for the client
		this.c_listener = new ClientListener(this.config, this);
		this.threads.execute(this.c_listener);

		this.creditscount = 0;
		this.user = "";
		this.loggedin = false;

	}
	public ProxyImpl(){
		this.config = new Config("Proxy");
		this.userconfig = new Config("user");
		this.creditscount = 0;
		this.user = "";
		this.loggedin = false;
	}

	@Override
	public LoginResponse login(LoginRequest request) throws IOException {
		if (loggedin) {
			return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS);
		}

		// TODO: anders den Usernamen bekommen
		user = request.getUsername();
		String pw = request.getPassword();
		// TODO: wie kann ich die pw vergleichen? dazu brauch ich alle namen,
		// die regestriert sind. Maxi: reg ausdrücke, um nur an die zahlen im
		// config zu kommen
		String confpw = "";
		try {
			confpw = "" + userconfig.getString(user + ".password");
		} catch (Exception e) {
			return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS);
		}

		if (pw.equals(confpw)) {
			boolean old = false;
			//does the name already exist?
			for (UserInfo u : users) {
				if (u.getName().equals(user))
					old = true;
			}
			
			//if not, add it to the users
			if (!old) {
				creditscount = userconfig.getInt(user
						+ ".credits");
				UserInfo ui = new UserInfo(user,creditscount, true);
				users.add(ui);
			} 
			// if it does exist, update the online status
			else{
				for(UserInfo u :users){
					if(u.getName().equals(user)){
						creditscount = u.getCredits();
						users.remove(u);
						users.add(new UserInfo(user,creditscount,true));
						break;
					}
				}
			}

			loggedin = true;
			return new LoginResponse(LoginResponse.Type.SUCCESS);
		} else {
			user = "";
			return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS);
		}
	}

	@Override
	public Response credits() throws IOException {
		if (!loggedin)
			return new MessageResponse("You have to log in");
		return new CreditsResponse(creditscount);
	}

	@Override
	public Response buy(BuyRequest credits) throws IOException {
		if (!loggedin)
			return new MessageResponse("You have to log in");
		creditscount += credits.getCredits();
		for(UserInfo u :users){
			if(u.getName().equals(user)){
				users.remove(u);
				users.add(new UserInfo(user,creditscount,true));
				break;
			}
		}
		
		return new BuyResponse(creditscount);
	}

	@Override
	public Response list() throws IOException {
		if (!loggedin)
			return new MessageResponse("You have to log in");
		
		//TODO: wie bekommt man die files?
		ArrayList <FileServerInfo> fsi = s_listener.getServers();
		Set<String> names = new HashSet<String>();
		for(FileServerInfo f : fsi){
			names.add(""+f.getPort());
		}
		return new ListResponse(names);
	}

	@Override
	public Response download(DownloadTicketRequest request) throws IOException {
		if (!loggedin)
			return new MessageResponse("You have to log in");
		
		ArrayList<FileServerInfo> files = s_listener.getServers();
		
		//TODO: name gueltig! woher die liste?? siehe list()
//		if(filelist.contains(request.getFilename()){
//			return new MessageResponse("Invalid file name");
//		}
		//TODO: genug credits
//		if(file.getBytes().length < creditscount){
//			return new MessageResponse("Not enough credits available");
//		}
		//which server
		int usage = 0;
		FileServerInfo fsi = new FileServerInfo(null, 0, 0, false);
		for(FileServerInfo f: files){
			if(f.isOnline()){
				if(usage >= f.getUsage()){
					fsi = f;
					usage = (int) f.getUsage();
				}
			}
		}
		//TODO: credits abziehen
		
		//create response
		DownloadTicket dt = new DownloadTicket(user,request.getFilename(), ChecksumUtils.generateChecksum(user, request.getFilename(), 0, /*file size*/request.getFilename().getBytes().length),fsi.getAddress(),fsi.getPort());
		return new DownloadTicketResponse(dt);
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageResponse logout() throws IOException {
		if (!loggedin)
			return new MessageResponse("You have to log in");
		for(UserInfo u :users){
			if(u.getName().equals(user)){
				users.remove(u);
				users.add(new UserInfo(user,creditscount,false));
				break;
			}
		}
		String log = user;
		user = "";
		loggedin = false;
		creditscount = 0;
		return new MessageResponse(log + " logged out");
	}

	@Override
	public void close() throws IOException {
		threads.shutdownNow();
		c_listener.close();
		s_listener.close();

	}

	public Response fileservers() {
		return new FileServerInfoResponse(s_listener.getServers());
	}

	public Response users() {
		return new UserInfoResponse(users);
	}

}

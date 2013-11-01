package proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ResourceBundle;


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
	private Config config;
	private ServerListenerUDP s_listener;
	private ClientAccept c_accept;
	
	private ArrayList<UserInfo> users;
	private ArrayList<FileServerInfo> servers;

	// thread pool
	private ExecutorService threads = Executors.newCachedThreadPool();

	private ResourceBundle bundle;

	public ProxyCli(Config config, Shell shell) throws SocketException {
		
		this.shell = shell;
		this.config = config;
		this.bundle = null;
		
		// register the shell
		this.shell.register(this);
		this.threads.execute(this.shell);
		
		// thread pool for the servers
		this.s_listener = new ServerListenerUDP(this.config);
		this.threads.execute(this.s_listener);
		
		// thread pool for the client
		//this.c_listener = new ClientListenerTCP(this.config, this);
		//this.threads.execute(this.c_listener);

		this.users = new ArrayList<UserInfo>();
		setUserList(); 
		setServerList();
		this.c_accept = new ClientAccept(this.config, this);
		this.threads.execute(this.c_accept);
		
		
	}

	@Override
	@Command
	public Response fileservers() throws IOException {
		setServerList();
		return new FileServerInfoResponse(servers);
	}	

	@Override
	@Command
	public Response users() throws IOException {
		return new UserInfoResponse(users);
	}

	@Override
	@Command
	public MessageResponse exit() throws IOException {
		c_accept.close();
		s_listener.close();
		threads.shutdown();
		shell.close();
		return null;
	}
	
	
	//user methods
	public void addUser(UserInfo user){
		users.add(user);
	}
	public void removeUser(UserInfo user){
		users.remove(user);
	}
	public ArrayList<UserInfo> getUserList(){
		return users;
	}
	
	//get all known users
	private void setUserList(){
		
		bundle = ResourceBundle.getBundle("user");
		Config userconfig = new Config("user");
		
		Enumeration<String> e = bundle.getKeys();
		ArrayList<String> usernames = new ArrayList<String>();
		while(e.hasMoreElements()){
			String next = e.nextElement().toString();
			String subs = next.substring(0,next.indexOf("."));
			if(!usernames.contains(subs)){
				usernames.add(subs);
			}
		}
		for(String s: usernames){
			users.add(new UserInfo(s, userconfig.getInt(s+".credits"),false));
		}
		
	}
	
	//server methods
	private void setServerList(){
		servers = s_listener.getServers();
	}
	
	public ArrayList<FileServerInfo> getServerList(){
		return servers;
	}
	
	public void addServer(FileServerInfo f){
		servers.add(f);
	}
	
	public void removeServer(FileServerInfo f){
		servers.remove(f);
	}
	
	public FileServerInfo getOnlineServer(){
		FileServerInfo server = new FileServerInfo(null, 0, 0, false);
		long usage = Long.MAX_VALUE;
		for(FileServerInfo f: servers){
			if(f.isOnline()){
				if(usage > f.getUsage()){
					usage = f.getUsage();
					server = f;
				}
			}
		}
		return server;
	}
	public boolean checkOnline(){
		for(FileServerInfo f: servers){
			if(f.isOnline()){
				return true;
			}
		}
		return false;
	}

}

package proxy;

import java.io.IOException;
import java.net.ServerSocket;
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
	private Config config;
	private ServerListenerUDP s_listener;
	private ClientListenerTCP c_listener;
	private ClientAccept c_accept;
	private ServerSocket s;
	
	private ArrayList<UserInfo> users;

	// thread pool
	private ExecutorService threads = Executors.newCachedThreadPool();


	public ProxyCli(Config config, Shell shell) throws SocketException {
		this.shell = shell;
		this.config = config;
		// register the shell
		this.shell.register(this);
		this.threads.execute(this.shell);
		// thread pool for the servers
		this.s_listener = new ServerListenerUDP(this.config);
		this.threads.execute(this.s_listener);
		// thread pool for the client
		this.c_listener = new ClientListenerTCP(this.config, this);
		this.threads.execute(this.c_listener);
		//proxy = new ProxyImpl(config);
		
		//this.c_accept = new ClientAccept(this.config, this,s);
		
		
		this.users = new ArrayList<UserInfo>();
	}

	@Override
	@Command
	public Response fileservers() throws IOException {
		return new FileServerInfoResponse(s_listener.getServers());
	}	

	@Override
	@Command
	public Response users() throws IOException {
		return new UserInfoResponse(users);
	}

	@Override
	@Command
	public MessageResponse exit() throws IOException {
		// FEHLER
		threads.shutdown();
		shell.close();
		return null;
	}
	
	public ServerListenerUDP getServerListener(){
		return s_listener;
	}
	public void setUsers(ArrayList<UserInfo> users){
		this.users = users;
	}
	public FileServerInfo getOnlineServer(){
		FileServerInfo server = new FileServerInfo(null, 0, 0, false);
		int usage = Integer.MAX_VALUE;
		for(FileServerInfo f: s_listener.getServers()){
			if(f.isOnline()){
				if(usage > f.getUsage()){
					server = f;
				}
			}
		}
		return server;
	}
	public boolean checkOnline(){
		for(FileServerInfo f: s_listener.getServers()){
			if(f.isOnline()){
				return true;
			}
		}
		return false;
	}

}

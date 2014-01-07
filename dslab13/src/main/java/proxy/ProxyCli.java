package proxy;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import message.Response;
import message.response.FileServerInfoResponse;
import message.response.MessageResponse;
import message.response.UserInfoResponse;
import model.FileServerInfo;
import model.Subscriber;
import model.UserInfo;

import org.bouncycastle.openssl.*;

import util.Config;
import cli.Command;
import cli.Shell;

public class ProxyCli implements IProxyCli {

	private Shell shell;
	private Config config;
	private ServerListenerUDP s_listener;
	private ClientAccept c_accept;

	private ArrayList<UserInfo> users;
	private List<FileServerInfo> servers;

	private ExecutorService threads = Executors.newCachedThreadPool();

	private ResourceBundle bundle;

	private ProxyRMI rmi;

	private Map<String, Integer> downloadstats;
	private List<Subscriber> subscribers;

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

		this.users = new ArrayList<UserInfo>();
		setUserList();
		setServerList();
		this.c_accept = new ClientAccept(this.config, this);
		this.threads.execute(this.c_accept);

		// start rmi
		this.rmi = new ProxyRMI(this);

		this.downloadstats = Collections.synchronizedMap(new HashMap<String, Integer>());
		this.subscribers = Collections.synchronizedList(new ArrayList<Subscriber>());

	}

	public PublicKey getConfigPublicKey() {
		String path = config.getString("keys.dir");
		PublicKey pkey = null;
		try {
			PEMReader in = new PEMReader(new FileReader(path + "/proxy.pub.pem"));
			pkey = (PublicKey) in.readObject();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pkey;
	}
	
	public boolean setConfigPublicKey(String username, PublicKey pkey){
		String path = config.getString("keys.dir");
		try {
			PEMWriter out = new PEMWriter(new FileWriter(path + "/"+ username+".pub.pem"));
			out.writeObject(pkey);
			out.flush();
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
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
		rmi.close();
		c_accept.close();
		s_listener.close();
		threads.shutdownNow();
		shell.close();
		return null;
	}

	// TODO delete this
	@Command
	public Response test() {
		String ret = "Subscribers";
		boolean sub = false;
		for (Subscriber s : subscribers) {
			sub = true;
			ret += "\n" + s.getFilename() + " " + s.getNumberOfDownloads();
		}
		if (!sub) {
			ret += "\n" + "no subscribers found..";
		}
		return new MessageResponse(ret);
	}

	// user methods
	public void addUser(UserInfo user) {
		users.add(user);
	}

	public void removeUser(UserInfo user) {
		users.remove(user);
	}

	public ArrayList<UserInfo> getUserList() {
		return users;
	}

	// get all known users
	private void setUserList() {

		bundle = ResourceBundle.getBundle("user");
		Config userconfig = new Config("user");

		Enumeration<String> e = bundle.getKeys();
		ArrayList<String> usernames = new ArrayList<String>();
		while (e.hasMoreElements()) {
			String next = e.nextElement().toString();
			String subs = next.substring(0, next.indexOf("."));
			if (!usernames.contains(subs)) {
				usernames.add(subs);
			}
		}
		for (String s : usernames) {
			users.add(new UserInfo(s, userconfig.getInt(s + ".credits"), false));
		}

	}

	// server methods
	private void setServerList() {
		servers = s_listener.getServers();
	}

	public List<FileServerInfo> getServerList() {
		return servers;
	}

	public void changeServer(FileServerInfo f) {
		s_listener.changeServer(f);
	}

	// subscriber methods
	public void addSubscriber(Subscriber s) {
		subscribers.add(s);
		long nod = 0;
		if (downloadstats.containsKey(s.getFilename())) {
			nod = downloadstats.get(s.getFilename());
		}
		checkSubscriber(s.getFilename(), nod);
	}

	public void removeSubscriber(Subscriber s) {
		subscribers.remove(s);
	}

	// checks if the numberofdownloads is reached
	public void checkSubscriber(String filename, long numberOfDownloads) {
		Subscriber sub = new Subscriber();
		boolean found = false;
		synchronized (subscribers) {
			for (Subscriber s : subscribers) {
				if (s.getFilename().equals(filename)) {
					if (s.getNumberOfDownloads() <= numberOfDownloads) {
						s.notifyClient();
						sub = s;
						found = true;
						break;
					}
				}
			}
		}
		if (found) {
			removeSubscriber(sub);
			found = false;

		}
	}

	// other methods
	public FileServerInfo getOnlineServer() {
		FileServerInfo server = new FileServerInfo(null, 0, 0, false);
		long usage = Long.MAX_VALUE;
		for (FileServerInfo f : servers) {
			if (f.isOnline()) {
				if (usage > f.getUsage()) {
					usage = f.getUsage();
					server = f;
				}
			}
		}
		return server;
	}

	public boolean checkOnline() {
		setServerList();
		for (FileServerInfo f : servers) {
			if (f.isOnline()) {
				return true;
			}
		}
		return false;
	}

	@Command
	public int getReadQuorum() {
		int onlineServer = 0;
		for (FileServerInfo fsi : servers) {
			if (fsi.isOnline()) {
				onlineServer++;
			}
		}
		int ret = (int) Math.ceil(onlineServer / 2d);

		if (onlineServer == 1) {
			return 1;
		} else if (onlineServer == 3) {
			return 2;
		} else {
			return ret;
		}
	}

	@Command
	public int getWriteQuorum() {
		int onlineServer = 0;
		for (FileServerInfo fsi : servers) {
			if (fsi.isOnline()) {
				onlineServer++;
			}
		}
		int ret = (int) (Math.ceil(onlineServer / 2d) + 1d);

		if (onlineServer == 1) {
			return 1;
		} else if (onlineServer == 3) {
			return 2;
		} else {
			return ret;
		}

	}

	// new download - update stats
	public void updateStats(String filename) {
		if (downloadstats.containsKey(filename)) {
			Integer i = downloadstats.get(filename);
			i++;
			downloadstats.put(filename, i);
			checkSubscriber(filename, i);
		} else {
			downloadstats.put(filename, 1);
			checkSubscriber(filename, 1);
		}
	}

	public Map<String, Integer> getDownloadStats() {
		return downloadstats;
	}

}

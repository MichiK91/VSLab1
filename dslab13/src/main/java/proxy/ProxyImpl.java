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

	private ServerListenerUDP s_listener;

	private ClientListenerTCP c_listener;

	private String user;
	private long creditscount;

	private boolean loggedin;
	private ProxyCli proxycli;
	private ServerSenderTCP ss;

	private ArrayList<UserInfo> users = new ArrayList<UserInfo>();

	public ProxyImpl(Config config, ProxyCli proxycli) throws SocketException {
		this.config = config;
		this.userconfig = new Config("user");
		this.proxycli = proxycli;
		// // thread pool for the servers
		// this.s_listener = new ServerListener(this.config);
		// this.threads.execute(this.s_listener);
		// // thread pool for the client
		// this.c_listener = new ClientListener(this.config, this);
		// this.threads.execute(this.c_listener);
		this.s_listener = proxycli.getServerListener();
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
			// does the name already exist?
			for (UserInfo u : users) {
				if (u.getName().equals(user))
					old = true;
			}

			// if not, add it to the users
			if (!old) {
				creditscount = userconfig.getInt(user + ".credits");
				UserInfo ui = new UserInfo(user, creditscount, true);
				users.add(ui);
				proxycli.setUsers(users);
			}
			// if it does exist, update the online status
			else {
				for (UserInfo u : users) {
					if (u.getName().equals(user)) {
						creditscount = u.getCredits();
						users.remove(u);
						users.add(new UserInfo(user, creditscount, true));
						proxycli.setUsers(users);
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
		for (UserInfo u : users) {
			if (u.getName().equals(user)) {
				users.remove(u);
				users.add(new UserInfo(user, creditscount, true));
				proxycli.setUsers(users);
				break;
			}
		}

		return new BuyResponse(creditscount);
	}

	@Override
	public Response list() throws IOException {
		if (!loggedin)
			return new MessageResponse("You have to log in");

		ServerSenderTCP ss = new ServerSenderTCP(proxycli.getOnlineServer()
				.getAddress(), proxycli.getOnlineServer().getPort());
		ListResponse res = (ListResponse) ss.send(new ListRequest());

		// TODO: wie bekommt man die files?
		// ArrayList<FileServerInfo> fsi = s_listener.getServers();
		// Set<String> names = new HashSet<String>();
		// for (FileServerInfo f : fsi) {
		// names.add("" + f.getPort());
		// }
		return res;
	}

	@Override
	public Response download(DownloadTicketRequest request) throws IOException {
		// TODO no file servers available
		if (!loggedin)
			return new MessageResponse("You have to log in");

		ArrayList<FileServerInfo> files = s_listener.getServers();
		String filename = request.getFilename();

		// no servers online
		if (!proxycli.checkOnline()) {
			return new MessageResponse("No servers online");
		}

		// get list of names
		FileServerInfo server = proxycli.getOnlineServer();
		ss = new ServerSenderTCP(server.getAddress(),
				server.getPort());
		ListResponse lres = (ListResponse) ss.send(new ListRequest());
		Set<String> names = lres.getFileNames();
		if (!names.contains(filename)) {
			return new MessageResponse("Invalid file name");
		}

		// check credits and reduce them
		InfoResponse ires = (InfoResponse) ss.send(new InfoRequest(filename));
		if (ires.getSize() > creditscount) {
			return new MessageResponse("Not enough credits available");
		} else{
			creditscount -= ires.getSize();
			for (UserInfo u : users) {
				if (u.getName().equals(user)) {
					users.remove(u);
					users.add(new UserInfo(user, creditscount, true));
					proxycli.setUsers(users);
					break;
				}
			}
		}
		
		VersionResponse vres = (VersionResponse) ss.send(new VersionRequest(filename));
		String csum = ChecksumUtils.generateChecksum(user, filename, vres.getVersion(), ires.getSize());
		
		// create response
		DownloadTicket dt = new DownloadTicket(user,filename,csum,server.getAddress(),server.getPort());
		
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
		for (UserInfo u : users) {
			if (u.getName().equals(user)) {
				users.remove(u);
				users.add(new UserInfo(user, creditscount, false));
				proxycli.setUsers(users);
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
		ss.close();
		threads.shutdownNow();
		c_listener.close();
		s_listener.close();
		

	}

}

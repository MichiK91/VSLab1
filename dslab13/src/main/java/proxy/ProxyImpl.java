package proxy;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import util.ChecksumUtils;
import util.Config;

import message.Response;
import message.request.*;
import message.response.*;
import model.DownloadTicket;
import model.FileServerInfo;
import model.UserInfo;

public class ProxyImpl implements IProxy, Closeable {

	private Config userconfig;

	private String user;
	private long creditscount;

	private boolean loggedin;
	private ProxyCli proxycli;
	private ServerSenderTCP ss;


	public ProxyImpl(ProxyCli proxycli) {
		this.userconfig = new Config("user");
		this.proxycli = proxycli;
		this.creditscount = 0;
		this.user = "";
		this.loggedin = false;

	}

	@Override
	public LoginResponse login(LoginRequest request) throws IOException {
		// user already logged in on this client
		if (loggedin) {
			return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS);
		}

		user = request.getUsername();
		String pw = request.getPassword();
		String confpw = "";

		boolean exist = false;
		// get password from config
		for (UserInfo u : proxycli.getUserList()) {
			if (user.equals(u.getName())) {
				confpw = "" + userconfig.getString(user + ".password");
				exist = true;
				break;
			}
		}
		// delivered user does not exist
		if (!exist) {
			return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS);
		}

		if (pw.equals(confpw)) {
			for (UserInfo u : proxycli.getUserList()) {
				if (u.getName().equals(user)) {
					// user already in list as online (other client)
					if (u.isOnline() == true) {
						return new LoginResponse(
								LoginResponse.Type.WRONG_CREDENTIALS);
					} else if (u.getName().equals(user)) {
						// change online status
						creditscount = u.getCredits();
						proxycli.removeUser(u);
						proxycli.addUser(new UserInfo(user, creditscount, true));
						break;
					}
				}
			}

			loggedin = true;
			return new LoginResponse(LoginResponse.Type.SUCCESS);
		} else {
			// wrong password
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
		for (UserInfo u : proxycli.getUserList()) {
			if (u.getName().equals(user)) {
				proxycli.removeUser(u);
				proxycli.addUser(new UserInfo(user, creditscount, true));
				break;
			}
		}

		return new BuyResponse(creditscount);
	}

	@Override
	public Response list() throws IOException {
		if (!loggedin)
			return new MessageResponse("You have to log in");
		// no servers online
		if (!proxycli.checkOnline()) 
			return new MessageResponse("No servers online");

		ServerSenderTCP ss = new ServerSenderTCP(proxycli.getOnlineServer()
				.getAddress(), proxycli.getOnlineServer().getPort());
		ListResponse res = (ListResponse) ss.send(new ListRequest());

		return res;
	}

	@Override
	public Response download(DownloadTicketRequest request) throws IOException {

		if (!loggedin)
			return new MessageResponse("You have to log in");

		String filename = request.getFilename();

		// no servers online
		if (!proxycli.checkOnline()) {
			return new MessageResponse("No servers online");
		}

		// get list of names
		FileServerInfo server = proxycli.getOnlineServer();
		ss = new ServerSenderTCP(server.getAddress(), server.getPort());
		ListResponse lres = (ListResponse) ss.send(new ListRequest());
		Set<String> names = null;
		try{
			names = lres.getFileNames();
		} catch(Exception e){
			//server has gone offline in meantime
			//sent to fast
			return null;
		}
		if (!names.contains(filename)) {
			return new MessageResponse("Invalid file name");
		}

		// check credits and reduce them
		InfoResponse ires = (InfoResponse) ss.send(new InfoRequest(filename));
		if (ires.getSize() > creditscount) {
			return new MessageResponse("Not enough credits available");
		} else {
			creditscount -= ires.getSize();
			// increase usage
			proxycli.changeServer(new FileServerInfo(server.getAddress(), server
			.getPort(), server.getUsage() + ires.getSize(), true));
			
			for (UserInfo u : proxycli.getUserList()) {
				if (u.getName().equals(user)) {
					proxycli.removeUser(u);
					proxycli.addUser(new UserInfo(user, creditscount, true));
					break;
				}
			}
		}

		VersionResponse vres = (VersionResponse) ss.send(new VersionRequest(
				filename));
		String csum = ChecksumUtils.generateChecksum(user, filename,
				vres.getVersion(), ires.getSize());

		// create response
		DownloadTicket dt = new DownloadTicket(user, filename, csum,
				server.getAddress(), server.getPort());

		return new DownloadTicketResponse(dt);
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {
		if (!loggedin)
			return new MessageResponse("You have to log in");

		// check if any server is online
		if (!proxycli.checkOnline())
			return new MessageResponse(
					"No servers online, failed to upload the file");

		ServerSenderTCP sstcp;
		List<FileServerInfo> servers = proxycli.getServerList();
		MessageResponse ures = null;

		for (FileServerInfo fs : servers) {
			// get all servers, which are online and send each of them a
			// uploadrequest
			if (fs.isOnline()) {
				sstcp = new ServerSenderTCP(fs.getAddress(), fs.getPort());
				ures = (MessageResponse) sstcp.send(request);
			}
		}

		// increase credits
		for (UserInfo u : proxycli.getUserList()) {
			if (user.equals(u.getName())) {
				creditscount += 2 * (request.getContent().length);
				proxycli.removeUser(u);
				proxycli.addUser(new UserInfo(user, creditscount, true));
			}
		}

		return ures;
	}

	@Override
	public synchronized MessageResponse logout() throws IOException {
		if (!loggedin)
			return new MessageResponse("You have to log in");
		for (UserInfo u : proxycli.getUserList()) {
			if (u.getName().equals(user)) {
				proxycli.removeUser(u);
				proxycli.addUser(new UserInfo(user, creditscount, false));
				// proxycli.setUsers(users);
				break;
			}
		}
		user = "";
		loggedin = false;
		creditscount = 0;
		return new MessageResponse("Successfully logged out.");
	}

	@Override
	public void close() throws IOException {
		if(ss != null)
			ss.close();

	}

}

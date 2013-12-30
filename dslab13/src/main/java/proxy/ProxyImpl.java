package proxy;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import message.Response;
import message.request.BuyRequest;
import message.request.DownloadTicketRequest;
import message.request.InfoRequest;
import message.request.ListRequest;
import message.request.LoginRequest;
import message.request.UploadRequest;
import message.request.VersionRequest;
import message.response.BuyResponse;
import message.response.CreditsResponse;
import message.response.DownloadTicketResponse;
import message.response.FileServerInfoResponse;
import message.response.InfoResponse;
import message.response.ListResponse;
import message.response.LoginResponse;
import message.response.MessageResponse;
import message.response.VersionResponse;
import model.DownloadTicket;
import model.FileServerInfo;
import model.UserInfo;
import util.ChecksumUtils;
import util.Config;

public class ProxyImpl implements IProxy, Closeable {

	private Config userconfig;

	private String user;
	private long creditscount;

	private boolean loggedin;
	private ProxyCli proxycli;
	private ServerSenderTCP ss;
	private int readQuorum = -1;
	private int writeQuorum = -1;
	
	

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
						return new LoginResponse(LoginResponse.Type.WRONG_CREDENTIALS);
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

		ServerSenderTCP ss = new ServerSenderTCP(proxycli.getOnlineServer().getAddress(), proxycli.getOnlineServer().getPort());
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

		// get latest Version
		int version = getLatestVersion(filename).getVersion();

		if (readQuorum < 0) {
			readQuorum = proxycli.getReadQuroum();
		}

		// get list of names
		// System.out.println("asdf");
		// System.out.println("readQuorum: " + getLowest(readQuorum));
		// System.out.println("after read");

		for (FileServerInfo fsi : getLowest(readQuorum)) {

			FileServerInfo server = fsi;
			ss = new ServerSenderTCP(server.getAddress(), server.getPort());
			ListResponse lres = (ListResponse) ss.send(new ListRequest());
			Set<String> names = null;
			try {
				names = lres.getFileNames();
			} catch (Exception e) {
				// server has gone offline in meantime
				// sent to fast
				return null;
			}
			if (!names.contains(filename)) {
				return new MessageResponse("Invalid file name");
			} else {
				VersionResponse vRes = (VersionResponse) ss.send(new VersionRequest(filename));
				if (version == vRes.getVersion()) {
					// check credits and reduce them
					InfoResponse ires = (InfoResponse) ss.send(new InfoRequest(filename));
					if (ires.getSize() > creditscount) {
						return new MessageResponse("Not enough credits available");
					} else {
						creditscount -= ires.getSize();
						// increase usage
						proxycli.changeServer(new FileServerInfo(server.getAddress(), server.getPort(), server.getUsage() + ires.getSize(), true));

						for (UserInfo u : proxycli.getUserList()) {
							if (u.getName().equals(user)) {
								proxycli.removeUser(u);
								proxycli.addUser(new UserInfo(user, creditscount, true));
								break;
							}
						}
					}

					VersionResponse vres = (VersionResponse) ss.send(new VersionRequest(filename));
					String csum = ChecksumUtils.generateChecksum(user, filename, vres.getVersion(), ires.getSize());

					// create response
					DownloadTicket dt = new DownloadTicket(user, filename, csum, server.getAddress(), server.getPort());
					
					//update statistics
					proxycli.updateStats(filename);

					return new DownloadTicketResponse(dt);
				}
			}
		}
		return null;
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {
		if (!loggedin)
			return new MessageResponse("You have to log in");

		// check if any server is online
		if (!proxycli.checkOnline())
			return new MessageResponse("No servers online, failed to upload the file");

		if (readQuorum < 0) {
			readQuorum = proxycli.getReadQuroum();
		}

		if (writeQuorum < 0) {
			writeQuorum = proxycli.getWriteQuorum();
		}

		ServerSenderTCP sstcp;
		List<FileServerInfo> servers = getLowest(writeQuorum);
		request = new UploadRequest(request.getFilename(), getLatestVersion(request.getFilename()).getVersion() + 1, request.getContent());
		MessageResponse ures = null;

		for (FileServerInfo fs : servers) {
			// TODO Stage1
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
		if (ss != null)
			ss.close();
	}

	private ArrayList<FileServerInfo> getLowest(int number) throws IOException {
		FileServerInfoResponse res = (FileServerInfoResponse) proxycli.fileservers();
		List<FileServerInfo> list = res.getFileServerInfo();

		Collections.sort(list, new Comparator<FileServerInfo>() {
			@Override
			public int compare(FileServerInfo fsi1, FileServerInfo fsi2) {
				if (fsi1.getUsage() < fsi2.getUsage()) {
					return -1;
				}
				if (fsi1.getUsage() == fsi2.getUsage()) {
					return 0;
				} else {
					return 1;
				}
			}
		});

		ArrayList<FileServerInfo> returnList = new ArrayList<FileServerInfo>();

		for (int i = 0; i < number; i++) {
			returnList.add(list.get(i));
		}

		return returnList;
	}

	private VersionResponse getLatestVersion(String name) throws IOException {
		int version = 0;
		for (FileServerInfo fsi : getLowest(readQuorum)) {
			VersionResponse vs = (VersionResponse) ss.send(new VersionRequest(name));
			if (vs.getVersion() > version) {
				version = vs.getVersion();
			}
		}
		return new VersionResponse(name, version);
	}

}

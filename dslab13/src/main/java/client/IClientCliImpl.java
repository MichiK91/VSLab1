package client;

import java.io.IOException;

import message.Response;
import message.response.LoginResponse;
import message.response.MessageResponse;

public class IClientCliImpl implements IClientCli {

	@Override
	public LoginResponse login(String username, String password)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response credits() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response buy(long credits) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response list() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response download(String filename) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageResponse upload(String filename) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageResponse logout() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageResponse exit() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}

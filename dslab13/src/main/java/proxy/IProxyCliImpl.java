package proxy;

import java.io.IOException;

import message.Response;
import message.response.MessageResponse;
import cli.Command;

public class IProxyCliImpl implements IProxyCli {

	@Override
	@Command
	public Response fileservers() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public Response users() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public MessageResponse exit() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}

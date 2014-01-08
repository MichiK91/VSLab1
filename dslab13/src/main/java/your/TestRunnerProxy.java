package your;

import proxy.ProxyCli;
import util.CliComponent;

public class TestRunnerProxy implements Runnable {

	private ProxyCli proxy;
	private CliComponent component;

	public TestRunnerProxy(ProxyCli proxy, CliComponent component) {
		this.proxy = proxy;
		this.component = component;
	}

	@Override
	public void run() {
		System.out.println("Proxy started");
	}

	public void exit() {
		component.getIn().addLine("!exit");
	}
}

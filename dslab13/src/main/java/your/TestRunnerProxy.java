package your;

import proxy.ProxyCli;

public class TestRunnerProxy implements Runnable {

	ProxyCli proxy;

	public TestRunnerProxy(ProxyCli proxy) {
		this.proxy = proxy;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}
}

package your;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import server.FileServerCli;
import util.CliComponent;
import util.ComponentFactory;
import util.Config;
import cli.Shell;
import cli.TestInputStream;
import cli.TestOutputStream;
import client.ClientCli;

public class MultiThreadingTest {

	private static ArrayList<TestRunnerClient> clientList = new ArrayList<TestRunnerClient>();
	private static ArrayList<TestRunnerFileServer> serverList = new ArrayList<TestRunnerFileServer>();
	private static TestInputStream in;
	private static TestOutputStream out = new TestOutputStream(System.out);
	private static Shell shell;
	private static ExecutorService threads;

	public static void main(String[] args) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Config config = new Config("loadtest");
		threads = Executors.newCachedThreadPool();
		ComponentFactory factory = new ComponentFactory();
		CliComponent component;

		int clients = config.getInt("clients");

		Class<?>[] ck = new Class<?>[] { Config.class, Shell.class };
		TestInputStream in = new TestInputStream();
		TestOutputStream out = new TestOutputStream(System.out);

		/*
		 * Shell shell1 = new Shell("Proxy", out, in); Method methodProxy = factory.getClass().getMethod("startProxy", ck); Object componentProxy = methodProxy.invoke(factory, new Config("Proxy"), shell1); component = new CliComponent(componentProxy, shell1, out, in); threads.execute(new TestRunnerProxy((ProxyCli) componentProxy, component));
		 */

		for (int i = 1; i < 6; i++) {
			shell = new Shell("FileServer", out, in);
			Method methodFS = factory.getClass().getMethod("startFileServer", ck);
			Object componentFS = methodFS.invoke(factory, new Config("fs" + i), shell);
			in = new TestInputStream();
			component = new CliComponent(componentFS, shell, out, in);
			TestRunnerFileServer runner = new TestRunnerFileServer((FileServerCli) componentFS, component);
			serverList.add(runner);
			threads.execute(runner);
		}

		for (int i = 0; i < clients; i++) {

			in = new TestInputStream();

			shell = new Shell("Client", out, in);

			Method methodClient = factory.getClass().getMethod("startClient", ck);
			Object componentClient = methodClient.invoke(factory, new Config("Client"), shell);
			component = new CliComponent(componentClient, shell, out, in);
			TestRunnerClient runner = new TestRunnerClient((ClientCli) componentClient, config, component);
			clientList.add(runner);
			threads.execute(runner);
		}
		exitAll();
	}

	private static void exitAll() {
		for (TestRunnerFileServer tr : serverList) {
			tr.exit();
		}

		for (TestRunnerClient tr : clientList) {
			tr.exit();
		}

		try {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		threads.shutdownNow();
	}
}

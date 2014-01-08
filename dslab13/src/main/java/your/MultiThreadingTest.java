package your;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

	public static void main(String[] args) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Config config = new Config("loadtest");
		ExecutorService threads = Executors.newCachedThreadPool();
		ComponentFactory factory = new ComponentFactory();

		int clients = config.getInt("clients");

		Class<?>[] ck = new Class<?>[] { Config.class, Shell.class };
		TestInputStream in = new TestInputStream();
		TestOutputStream out = new TestOutputStream(System.out);

		// Shell shell1 = new Shell("Proxy", out, in);
		// Method methodProxy = factory.getClass().getMethod("startProxy", ck);
		// Object componentProxy = methodProxy.invoke(factory, new Config("Proxy"), shell1);
		// threads.execute(new TestRunnerProxy((ProxyCli) componentProxy));

		for (int i = 1; i < 6; i++) {
			Shell shell = new Shell("FileServer", out, in);
			Method methodFS = factory.getClass().getMethod("startFileServer", ck);
			Object componentFS = methodFS.invoke(factory, new Config("fs" + i), shell);
			threads.execute(new TestRunnerFileServer((FileServerCli) componentFS));
		}

		for (int i = 0; i < clients; i++) {

			in = new TestInputStream();

			Shell shell = new Shell("Client", out, in);

			CliComponent component;

			Method methodClient = factory.getClass().getMethod("startClient", ck);
			Object componentClient = methodClient.invoke(factory, new Config("Client"), shell);
			component = new CliComponent(componentClient, shell, out, in);
			threads.execute(new TestRunnerClient((ClientCli) componentClient, config, component));

		}

	}
}

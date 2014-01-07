package your;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import util.ComponentFactory;
import util.Config;
import cli.Shell;
import cli.TestInputStream;
import cli.TestOutputStream;

public class MultiThreadingTest {

	public static void main(String[] args) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Config config = new Config("loadtest");
		Config clientConfig = new Config("client");
		ExecutorService threads = Executors.newCachedThreadPool();
		ComponentFactory factory = new ComponentFactory();

		int clients = config.getInt("clients");

		TestInputStream in = new TestInputStream();
		TestOutputStream out = new TestOutputStream(System.out);
		Shell shell = new Shell("Proxy", out, in);
		Class<?>[] ck = new Class<?>[] { Config.class, Shell.class };

		Method method1 = factory.getClass().getMethod("startProxy", ck);
		Object component1 = method1.invoke(factory, new Config("Proxy"), shell);

		for (int i = 0; i < clients; i++) {

			// Method method = factory.getClass().getMethod("startClient", ck);
			// Object component = method.invoke(factory, new Config("Client"), shell);
			// threads.execute(new TestRunner(config.getInt("downloadsPerMin"), config.getInt("uploadsPerMin"), config.getInt("fileSize"), config.getString("overwriteRatio")));
			// System.out.println(i);
		}

		/*
		 * CliComponent component; ComponentFactory factory = new ComponentFactory(); Map<String, CliComponent> componentMap = new HashMap<String, CliComponent>();
		 * 
		 * Map<String, Class<?>[]> mapping = new HashMap<String, Class<?>[]>(); mapping.put("startClient", new Class<?>[] { Config.class, Shell.class }); mapping.put("startProxy", new Class<?>[] { Config.class, Shell.class }); mapping.put("startFileServer", new Class<?>[] { Config.class, Shell.class });
		 * 
		 * // ------------------ String methode = "download"; // ------------------
		 */
	}
}

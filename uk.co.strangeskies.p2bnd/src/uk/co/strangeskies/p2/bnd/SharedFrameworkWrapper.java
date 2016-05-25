package uk.co.strangeskies.p2.bnd;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.jar.Manifest;
import java.util.stream.StreamSupport;

import uk.co.strangeskies.osgi.frameworkwrapper.FrameworkWrapper;
import uk.co.strangeskies.p2.P2Repository;
import uk.co.strangeskies.p2.P2RepositoryFactory;
import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.Log.Level;
import uk.co.strangeskies.utilities.classpath.ContextClassLoaderRunner;
import uk.co.strangeskies.utilities.classpath.ManifestUtilities;
import uk.co.strangeskies.utilities.function.ThrowingFunction;

public class SharedFrameworkWrapper {
	private final int frameworkTimeoutMilliseconds;
	private final int serviceTimeoutMilliseconds;
	private final Map<String, String> frameworkProperties;

	private FrameworkWrapper frameworkWrapper;
	private P2RepositoryFactory repositoryFactory;
	private URLClassLoader classLoader;

	private Map<Object, P2Repository> openConnections = new WeakHashMap<>();

	public SharedFrameworkWrapper(int frameworkTimeoutMilliseconds, int serviceTimeoutMilliseconds,
			Map<String, String> frameworkProperties) {
		this.frameworkTimeoutMilliseconds = frameworkTimeoutMilliseconds;
		this.serviceTimeoutMilliseconds = serviceTimeoutMilliseconds;
		this.frameworkProperties = new HashMap<>(frameworkProperties);
	}

	public synchronized <T> T withConnection(Object key, Log log, ThrowingFunction<P2Repository, T, ?> action) {
		initialise(log);

		return frameworkWrapper.withFramework(() -> {
			P2Repository repository = openConnections.get(key);

			if (repository == null) {
				repository = repositoryFactory.get(log);
				openConnections.put(key, repository);
			}

			return action.apply(repository);
		});
	}

	public synchronized void initialise(Log log) {
		if (frameworkWrapper == null) {
			try {
				Manifest manifest = ManifestUtilities.getManifest(P2BndRepository.class);

				log.log(Level.INFO, "Setting framework URL");
				String frameworkJars = manifest.getMainAttributes().getValue(FrameworkWrapper.EMBEDDED_FRAMEWORK);
				Set<URL> frameworkUrls = new HashSet<>();
				try {
					for (String frameworkJar : frameworkJars.split(",")) {
						File frameworkFile = File.createTempFile("framework", ".jar");
						frameworkFile.deleteOnExit();
						frameworkUrls.add(frameworkFile.toURI().toURL());

						try (InputStream input = P2BndRepository.class.getClassLoader().getResourceAsStream(frameworkJar);
								OutputStream output = new FileOutputStream(frameworkFile)) {
							while (input.available() > 0) {
								output.write(input.read());
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}

				log.log(Level.INFO, "Creating delegating classloader to " + frameworkUrls);
				classLoader = new URLClassLoader(frameworkUrls.toArray(new URL[frameworkUrls.size()]),
						P2BndRepository.class.getClassLoader());

				new ContextClassLoaderRunner(classLoader).run(() -> {
					log.log(Level.INFO, "Fetching framework wrapper service loader");
					ServiceLoader<FrameworkWrapper> serviceLoader = ServiceLoader.load(FrameworkWrapper.class, classLoader);

					log.log(Level.INFO, "Loading framework wrapper service");
					frameworkWrapper = StreamSupport.stream(serviceLoader.spliterator(), false).findAny().orElseThrow(
							() -> new RuntimeException("Cannot find service implementing " + FrameworkWrapper.class.getName()));
				});

				log.log(Level.INFO, "Initialise framework wrapper properties");
				frameworkWrapper.setLog(log);

				frameworkWrapper.setTimeoutMilliseconds(frameworkTimeoutMilliseconds);

				frameworkWrapper.setLaunchProperties(frameworkProperties);

				String bundleJars = manifest.getMainAttributes().getValue(FrameworkWrapper.EMBEDDED_RUNPATH);
				frameworkWrapper.setBundles(stream(bundleJars.split(",")).map(s -> "/" + s.trim())
						.collect(toMap(s -> "classpath:" + s, s -> () -> P2BndRepository.class.getResourceAsStream(s))));

				frameworkWrapper.setInitialisationAction(() -> {
					frameworkWrapper.withServiceThrowing(P2RepositoryFactory.class, p -> {
						repositoryFactory = p;
					}, serviceTimeoutMilliseconds);
				});

				frameworkWrapper.setShutdownAction(() -> {
					for (P2Repository repository : openConnections.values()) {
						cleanConnection(repository);
					}
					openConnections.clear();

					log.log(Level.INFO, "Closing framework");
					repositoryFactory = null;
					try {
						classLoader.close();
					} catch (IOException e) {
						log.log(Level.WARN, "Unable to close class loader", e);
					}
				});
			} catch (Throwable e) {
				log.log(Level.ERROR, "Could not initialise P2BndRepository", e);

				cleanFramework(log);

				throw e;
			}
		}
	}

	public synchronized void closeConnection(Object key) {
		P2Repository connection = openConnections.remove(key);

		if (connection != null) {
			cleanFramework(cleanConnection(connection));
		}
	}

	private Log cleanConnection(P2Repository connection) {
		Log log = connection.getLog();
		try {
			log.log(Level.INFO, "Closing connection " + connection.getName());
			connection.close();
		} catch (IOException e) {
			log.log(Level.WARN, "Unable to close P2Repository", e);
		}
		return log;
	}

	private void cleanFramework(Log log) {
		if (openConnections.isEmpty()) {
			try {
				frameworkWrapper.stopFramework();
			} catch (Exception e) {
				log.log(Level.WARN, "Unable to stop framework", e);
			}
		}
	}
}

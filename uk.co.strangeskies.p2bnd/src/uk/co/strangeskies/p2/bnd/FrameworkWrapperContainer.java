/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.p2bnd.
 *
 * uk.co.strangeskies.p2bnd is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.p2bnd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.p2bnd.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.p2.bnd;

import static java.util.Arrays.stream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.jar.Attributes;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import uk.co.strangeskies.osgi.frameworkwrapper.FrameworkWrapper;
import uk.co.strangeskies.p2.P2Repository;
import uk.co.strangeskies.p2.P2RepositoryFactory;
import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.Log.Level;
import uk.co.strangeskies.utilities.classpath.FilteringClassLoader;
import uk.co.strangeskies.utilities.classpath.ManifestUtilities;
import uk.co.strangeskies.utilities.function.ThrowingFunction;
import uk.co.strangeskies.utilities.function.ThrowingSupplier;

public class FrameworkWrapperContainer {
	public static final String EMBEDDED_FRAMEWORK = "Embedded-Framework";
	public static final String EMBEDDED_RUNPATH = "Embedded-Runpath";

	private static final String STRANGE_SKIES_PACKAGE = "uk.co.strangeskies";

	private final int frameworkTimeoutMilliseconds;
	private final int serviceTimeoutMilliseconds;
	private final Map<String, String> frameworkProperties;
	private final Set<URL> frameworkJars;
	private final Set<URL> bundleJars;

	private FrameworkWrapper frameworkWrapper;
	private P2RepositoryFactory repositoryFactory;
	private URLClassLoader classLoader;

	private Map<Object, P2Repository> openConnections = new WeakHashMap<>();

	public FrameworkWrapperContainer(int frameworkTimeoutMilliseconds, int serviceTimeoutMilliseconds,
			Map<String, String> frameworkProperties, Set<URL> frameworkJars, Set<URL> bundleJars) {
		this.frameworkTimeoutMilliseconds = frameworkTimeoutMilliseconds;
		this.serviceTimeoutMilliseconds = serviceTimeoutMilliseconds;
		this.frameworkProperties = new HashMap<>(frameworkProperties);

		this.frameworkJars = frameworkJars;
		this.bundleJars = bundleJars;
	}

	public FrameworkWrapperContainer(int frameworkTimeoutMilliseconds, int serviceTimeoutMilliseconds,
			Map<String, String> frameworkProperties) {
		this(frameworkTimeoutMilliseconds, serviceTimeoutMilliseconds, frameworkProperties,
				ManifestUtilities.getManifest(P2BndRepository.class).getMainAttributes());
	}

	private FrameworkWrapperContainer(int frameworkTimeoutMilliseconds, int serviceTimeoutMilliseconds,
			Map<String, String> frameworkProperties, Attributes attributes) {
		this(frameworkTimeoutMilliseconds, serviceTimeoutMilliseconds, frameworkProperties,
				toUrls(attributes.getValue(EMBEDDED_FRAMEWORK)), toUrls(attributes.getValue(EMBEDDED_RUNPATH)));
	}

	private static Set<URL> toUrls(String value) {
		return stream(value.split(",")).map(String::trim).map(P2BndRepository.class.getClassLoader()::getResource)
				.collect(Collectors.toSet());
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

	private boolean classDelegationFilter(Class<?> clazz) {
		List<String> packages = new ArrayList<>(P2BndRepository.FORWARD_PACKAGES);

		for (String forwardPackage : packages) {
			String packageName = clazz.getPackage().getName();
			if (packageName.startsWith(forwardPackage + ".") || packageName.equals(forwardPackage)) {
				return true;
			}
		}

		return false;
	}

	public synchronized void initialise(Log log) {
		if (frameworkWrapper == null) {
			try {
				log.log(Level.INFO, "Setting framework URL");
				Set<URL> frameworkUrls = new HashSet<>();
				try {
					for (URL frameworkJar : frameworkJars) {
						File frameworkFile = File.createTempFile("framework", ".jar");
						frameworkFile.deleteOnExit();
						frameworkUrls.add(frameworkFile.toURI().toURL());

						try (InputStream input = frameworkJar.openStream();
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
						new FilteringClassLoader(FrameworkWrapper.class.getClassLoader(), this::classDelegationFilter));

				/*
				 * 
				 * 
				 * 
				 * 
				 * 
				 * 
				 * 
				 * 
				 * 
				 * 
				 * TODO make parent classloader the bundle classloader!
				 * 
				 * 
				 * 
				 * 
				 * 
				 * 
				 * 
				 * 
				 * 
				 * 
				 */

				log.log(Level.INFO, "Fetching framework wrapper service loader");
				ServiceLoader<FrameworkWrapper> serviceLoader = ServiceLoader.load(FrameworkWrapper.class, classLoader);

				log.log(Level.INFO, "Loading framework wrapper service");
				frameworkWrapper = StreamSupport.stream(serviceLoader.spliterator(), false).findAny().orElseThrow(
						() -> new RuntimeException("Cannot find service implementing " + FrameworkWrapper.class.getName()));

				log.log(Level.INFO, "Initialise framework wrapper properties");
				frameworkWrapper.setLog(log);

				frameworkWrapper.setTimeoutMilliseconds(frameworkTimeoutMilliseconds);

				frameworkWrapper.setLaunchProperties(frameworkProperties);

				Map<String, ThrowingSupplier<InputStream, ?>> bundles = new LinkedHashMap<>();
				for (URL bundleJar : bundleJars) {
					bundles.put(bundleJar.toString(), bundleJar::openStream);
				}
				frameworkWrapper.setBundles(bundles);

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

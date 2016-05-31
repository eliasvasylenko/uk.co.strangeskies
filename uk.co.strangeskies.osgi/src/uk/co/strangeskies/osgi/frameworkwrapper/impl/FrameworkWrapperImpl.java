/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.osgi.
 *
 * uk.co.strangeskies.osgi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.osgi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.osgi.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.osgi.frameworkwrapper.impl;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static uk.co.strangeskies.utilities.classpath.ManifestUtilities.parseAttributes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import uk.co.strangeskies.osgi.frameworkwrapper.FrameworkWrapper;
import uk.co.strangeskies.osgi.frameworkwrapper.server.FrameworkWrapperServer;
import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.Log.Level;
import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.classpath.Attribute;
import uk.co.strangeskies.utilities.classpath.AttributeProperty;
import uk.co.strangeskies.utilities.classpath.FilteringClassLoader;
import uk.co.strangeskies.utilities.function.ThrowingConsumer;
import uk.co.strangeskies.utilities.function.ThrowingFunction;
import uk.co.strangeskies.utilities.function.ThrowingRunnable;
import uk.co.strangeskies.utilities.function.ThrowingSupplier;

/**
 * A framework wrapper implementation which delegates internally to a
 * {@link FrameworkWrapperServer} implementation.
 * 
 * @author Elias N Vasylenko
 */
public class FrameworkWrapperImpl implements FrameworkWrapper {
	protected class VersionedPackage {
		private final String packageName;
		private final String versionString;

		public VersionedPackage(String packageName, String versionString) {
			this.packageName = packageName;
			this.versionString = versionString;
		}

		public String packageName() {
			return packageName;
		}

		public String versionString() {
			return versionString;
		}

		@Override
		public String toString() {
			return packageName + ';' + VERSION_PROPERTY + "=\"" + versionString + '"';
		}
	}

	private static final String IMPORT_PACKAGE = "Import-Package";
	private static final String EXPORT_PACKAGE = "Export-Package";
	private static final String PRIVATE_PACKAGE = "Private-Package";
	private static final String FRAMEWORK_SYSTEMPACKAGES_EXTRA = "org.osgi.framework.system.packages.extra";

	protected static final Object VERSION_PROPERTY = "version";
	protected static final String DEFAULT_VERSION = "0.0.0";

	private Log log;
	private ClassLoader baseClassLoader;

	private Set<URL> frameworkJars;
	private Set<URL> bundleJars;
	private Set<VersionedPackage> versionedPackages;

	private FrameworkWrapper component;

	/**
	 * @param classLoader
	 *          the class loader on which to host the framework
	 * @param manifest
	 *          the manifest containing the
	 *          {@link FrameworkWrapper#EMBEDDED_FRAMEWORK} and
	 *          {@link FrameworkWrapper#EMBEDDED_RUNPATH} attributes
	 */
	public FrameworkWrapperImpl(ClassLoader classLoader, Manifest manifest) {
		Attributes attributes = manifest.getMainAttributes();

		setFrameworkJars(toUrls(classLoader, attributes.getValue(EMBEDDED_FRAMEWORK)));
		setBundleJars(toUrls(classLoader, attributes.getValue(EMBEDDED_RUNPATH)));
		setPackageVersions(getVersionedPackages(attributes, attributes.getValue(EMBEDDED_CLASSPATH)));

		setBaseClassLoader(new FilteringClassLoader(classLoader,
				c -> versionedPackages.stream().anyMatch(v -> v.packageName().equals(c.getPackage().getName()))));
	}

	protected FrameworkWrapperImpl() {}

	private Set<VersionedPackage> getVersionedPackages(Attributes attributes, String packagesString) {
		Set<String> packageNames = getPackageNames(packagesString);
		Set<VersionedPackage> versionedPackages = new HashSet<>();

		versionedPackages.addAll(getVersionedPackages(attributes.getValue(PRIVATE_PACKAGE)));
		versionedPackages.addAll(getVersionedPackages(attributes.getValue(IMPORT_PACKAGE)));
		versionedPackages.addAll(getVersionedPackages(attributes.getValue(EXPORT_PACKAGE)));

		return versionedPackages.stream().filter(p -> packageNames.contains(p.packageName())).collect(toSet());
	}

	protected Set<VersionedPackage> getVersionedPackages(String packageVersionsString) {
		if (packageVersionsString == null)
			return emptySet();

		List<Attribute> attributes = parseAttributes(packageVersionsString);

		return attributes.stream().map(a -> {
			AttributeProperty<?> property = a.properties().get(VERSION_PROPERTY);
			String propertyString = property == null ? DEFAULT_VERSION : property.value().toString();

			return new VersionedPackage(a.name(), propertyString);
		}).collect(toSet());
	}

	protected Set<String> getPackageNames(String packagesString) {
		return stream(packagesString.split(",")).map(String::trim).collect(toSet());
	}

	private String getVersionedPackagesString() {
		return versionedPackages.stream().map(Object::toString).collect(joining(","));
	}

	protected void setBaseClassLoader(ClassLoader baseClassLoader) {
		this.baseClassLoader = baseClassLoader;
	}

	protected Set<URL> toUrls(ClassLoader classLoader, String value) {
		return stream(value.split(",")).map(String::trim).map(classLoader::getResource).collect(Collectors.toSet());
	}

	protected void setFrameworkJars(Set<URL> frameworkJars) {
		this.frameworkJars = frameworkJars;
	}

	protected void setBundleJars(Set<URL> bundleJars) {
		this.bundleJars = bundleJars;
	}

	protected void setPackageVersions(Collection<? extends VersionedPackage> packageVersions) {
		this.versionedPackages = new HashSet<>(packageVersions);
	}

	@Override
	public void setTimeoutMilliseconds(int timeoutMilliseconds) {
		getComponent().setTimeoutMilliseconds(timeoutMilliseconds);
	}

	@Override
	public void setLog(Log log, boolean publishService) {
		this.log = log;
		getComponent().setLog(log, publishService);
	}

	@Override
	public void setLaunchProperties(Map<String, String> properties) {
		if (versionedPackages != null && !versionedPackages.isEmpty()) {
			properties = new HashMap<>(properties);

			properties.computeIfAbsent(FRAMEWORK_SYSTEMPACKAGES_EXTRA, k -> getVersionedPackagesString());
		}

		getComponent().setLaunchProperties(properties);
	}

	@Override
	public void setBundles(Map<String, ThrowingSupplier<InputStream, ?>> bundleSources) {
		getComponent().setBundles(bundleSources);
	}

	@Override
	public void startFramework() {
		getComponent().startFramework();
	}

	@Override
	public void stopFramework() {
		getComponent().stopFramework();
	}

	@Override
	public Observable<FrameworkWrapper> onStart() {
		return getComponent().onStart();
	}

	@Override
	public Observable<FrameworkWrapper> onStop() {
		return getComponent().onStop();
	}

	@Override
	public boolean isStarted() {
		return getComponent().isStarted();
	}

	private FrameworkWrapper getComponent() {
		if (component == null) {
			component = create();
			setLaunchProperties(emptyMap());
		}
		return component;
	}

	private FrameworkWrapper create() {
		FrameworkWrapper frameworkWrapper = null;

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
			URLClassLoader classLoader = new URLClassLoader(frameworkUrls.toArray(new URL[frameworkUrls.size()]),
					baseClassLoader);

			log.log(Level.INFO, "Fetching framework wrapper service loader");
			ServiceLoader<FrameworkWrapperServer> serviceLoader = ServiceLoader.load(FrameworkWrapperServer.class,
					classLoader);

			log.log(Level.INFO, "Loading framework wrapper service");
			frameworkWrapper = StreamSupport.stream(serviceLoader.spliterator(), false).findAny().orElseThrow(
					() -> new RuntimeException("Cannot find service implementing " + FrameworkWrapperServer.class.getName()));

			log.log(Level.INFO, "Initialise framework wrapper properties");
			frameworkWrapper.setLog(log, false);

			frameworkWrapper.onStop().addObserver(f -> {
				log.log(Level.INFO, "Closing framework");
				try {
					classLoader.close();
				} catch (IOException e) {
					log.log(Level.WARN, "Unable to close class loader", e);
				}
			});

			Map<String, ThrowingSupplier<InputStream, ?>> bundles = new LinkedHashMap<>();
			for (URL bundleJar : bundleJars) {
				bundles.put(bundleJar.toString(), bundleJar::openStream);
			}
			frameworkWrapper.setBundles(bundles);

			return frameworkWrapper;
		} catch (Throwable e) {
			log.log(Level.ERROR, "Could not initialise P2BndRepository", e);

			if (frameworkWrapper != null) {
				try {
					frameworkWrapper.stopFramework();
				} catch (Exception ignore) {}
			}

			throw e;
		}
	}

	@Override
	public <E extends Exception> void withFramework(ThrowingRunnable<E> action) throws E {
		getComponent().withFramework(action);
	}

	@Override
	public <R, E extends Exception> R withFramework(ThrowingSupplier<? extends R, E> action) throws E {
		return getComponent().withFramework(action);
	}

	@Override
	public <T, E extends Exception> void withService(Class<T> serviceClass, String filter,
			ThrowingConsumer<? super T, E> action, int timeoutMilliseconds) throws E {
		getComponent().withService(serviceClass, filter, action, timeoutMilliseconds);
	}

	@Override
	public <T, R, E extends Exception> R withService(Class<T> serviceClass, String filter,
			ThrowingFunction<? super T, ? extends R, E> action, int timeoutMilliseconds) throws E {
		return getComponent().withService(serviceClass, filter, action, timeoutMilliseconds);
	}
}

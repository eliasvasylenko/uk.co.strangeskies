/*
] * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;
import static org.osgi.framework.Constants.FRAMEWORK_BUNDLE_PARENT;
import static org.osgi.framework.Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.osgi.framework.Constants;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.service.repository.Repository;

import aQute.bnd.service.Plugin;
import aQute.bnd.service.RemoteRepositoryPlugin;
import aQute.bnd.service.ResourceHandle;
import aQute.bnd.service.Strategy;
import aQute.bnd.version.Version;
import aQute.service.reporter.Reporter;
import uk.co.strangeskies.bnd.ReporterLog;
import uk.co.strangeskies.p2.P2Repository;
import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.Log.Level;
import uk.co.strangeskies.utilities.classpath.FilteringClassLoader;
import uk.co.strangeskies.utilities.function.ThrowingFunction;

/**
 * This class is not primarily intended to be used within OSGi environments. For
 * an OSGi enabled implementation of {@link RemoteRepositoryPlugin} and
 * {@link Repository} which provides p2 repository support, the
 * {@code uk.co.strangeskies.p2.P2RepositoryImpl} class in the
 * {@code uk.co.strangeskies.p2} project should be used instead. This class is
 * simply a wrapper for that implementation for use in non OSGi environments,
 * and creates a framework internally to host the necessary Eclipse Project
 * bundles.
 * <p>
 *
 * @author Elias N Vasylenko
 */
public class P2BndRepository implements RemoteRepositoryPlugin, Repository, Plugin, Closeable

/*-, Refreshable, Actionable, RegistryPlugin, SearchableRepository, InfoRepository*/

{
	public static final int FRAMEWORK_TIMEOUT_MILLISECONDS = 4000;
	public static final int SERVICE_TIMEOUT_MILLISECONDS = 4000;

	private static final String OSGI_CLEAN = "osgi.clean";
	private static final String OSGI_CLEAR_PERSISTED_STATE = "clearPersistedState";

	public static final List<String> FORWARD_VERSIONED_PACKAGES = unmodifiableList(asList(
			"uk.co.strangeskies.osgi;version=\"1.0.0\"",

			/*
			 * TODO There are just for in-framework logging?
			 */
			"uk.co.strangeskies.osgi.frameworkwrapper;version=\"1.0.0\"",
			"uk.co.strangeskies.osgi.servicewrapper;version=\"1.0.0\"", "uk.co.strangeskies.utilities.text;version=\"1.0.0\"",
			"uk.co.strangeskies.utilities.classpath;version=\"1.0.0\"",
			"uk.co.strangeskies.utilities.collection;version=\"1.0.0\"",
			"uk.co.strangeskies.utilities.factory;version=\"1.0.0\"",
			"uk.co.strangeskies.utilities.flowcontrol;version=\"1.0.0\"",
			"uk.co.strangeskies.utilities.function;version=\"1.0.0\"", "uk.co.strangeskies.utilities.tuple;version=\"1.0.0\"",
			"uk.co.strangeskies.p2;version=\"1.0.0\"", "uk.co.strangeskies.p2.bnd;version=\"1.0.0\"",
			"org.osgi.service.repository;version=\"1.0.0\"", "org.osgi.resource;version=\"1.0.0\"",

			"uk.co.strangeskies.utilities;version=\"1.0.0\"", "uk.co.strangeskies.p2;version=\"1.0.0\"",
			"aQute.bnd.service;version=\"4.1.0\"", "aQute.bnd.version;version=\"1.3.0\"",
			"aQute.service.reporter;version=\"1.0.0\""));
	public static final List<String> FORWARD_PACKAGES = unmodifiableList(
			FORWARD_VERSIONED_PACKAGES.stream().map(s -> s.split(";")[0]).collect(Collectors.toList()));

	@SuppressWarnings("serial")
	public static final Map<String, String> FRAMEWORK_PROPERTIES = new HashMap<String, String>() {
		{
			put(OSGI_CLEAN, Boolean.toString(true));
			put(OSGI_CLEAR_PERSISTED_STATE, Boolean.toString(true));

			put(FRAMEWORK_SYSTEMPACKAGES_EXTRA, FORWARD_VERSIONED_PACKAGES.stream().collect(joining(",")));
			put(FRAMEWORK_BUNDLE_PARENT, Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK);
		}
	};

	private static FrameworkWrapperContainer SHARED_FRAMEWORK;

	public static void setSharedFramework(FrameworkWrapperContainer sharedFrameworkWrapper) {
		if (SHARED_FRAMEWORK != null)
			throw new IllegalStateException();
		SHARED_FRAMEWORK = sharedFrameworkWrapper;
	}

	public static FrameworkWrapperContainer getSharedFramework() {
		if (SHARED_FRAMEWORK == null)
			SHARED_FRAMEWORK = new FrameworkWrapperContainer(FRAMEWORK_TIMEOUT_MILLISECONDS, SERVICE_TIMEOUT_MILLISECONDS,
					FRAMEWORK_PROPERTIES,
					new FilteringClassLoader(P2BndRepository.class.getClassLoader(), P2BndRepository::classDelegationFilter));
		return SHARED_FRAMEWORK;
	}

	public static boolean classDelegationFilter(Class<?> clazz) {
		List<String> packages = new ArrayList<>(P2BndRepository.FORWARD_PACKAGES);

		for (String forwardPackage : packages) {
			String packageName = clazz.getPackage().getName();
			if (packageName.startsWith(forwardPackage + ".") || packageName.equals(forwardPackage)) {
				return true;
			}
		}

		return false;
	}

	private Log log = (l, s) -> {
		System.out.println(l + ": " + s);
	};

	private Map<String, String> properties;
	private Reporter reporter;

	public static void main(String... args) throws Exception {
		P2BndRepository first = test("hi");

		System.out.println(first.getName());

		first.close();
	}

	private static P2BndRepository test(String name) throws Exception {
		Map<String, String> map = new HashMap<>();
		map.put("name", name);
		map.put("location", "http://download.eclipse.org/releases/mars/");

		P2BndRepository repo = new P2BndRepository();
		repo.setLog(new Log() {

			@Override
			public void log(Level level, String message) {
				System.out.println(level + ": " + message);
			}

			@Override
			public void log(Level level, String message, Throwable exception) {
				Log.super.log(level, message, exception);
				exception.printStackTrace();
			}
		});

		repo.setProperties(map);

		return repo;
	}

	/**
	 * Create an unconfigured P2 repository accessible through Bnd.
	 */
	public P2BndRepository() {
		log.log(Level.INFO, "Creating P2BndRepository");
	}

	@Override
	public void close() throws IOException {
		getSharedFramework().closeConnection(this);
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	public void setLog(Log log) {
		this.log = log;
	}

	public Log getLog() {
		WeakReference<P2BndRepository> logSource = new WeakReference<>(this);

		return new Log() {
			@Override
			public void log(Level level, String message) {
				P2BndRepository repository = logSource.get();
				if (repository != null) {
					repository.log.log(level, message);
				}
			}

			@Override
			public void log(Level level, String message, Throwable exception) {
				P2BndRepository repository = logSource.get();
				if (repository != null) {
					repository.log.log(level, message, exception);
				}
			}
		};
	}

	@Override
	public void setProperties(Map<String, String> properties) throws Exception {
		log.log(Level.WARN, "Setting P2 repository properties: " + properties);

		this.properties = properties;
	}

	@Override
	public void setReporter(Reporter processor) {
		log.log(Level.WARN, "Setting P2 repository reporter");

		reporter = processor;

		log = new ReporterLog(reporter);
	}

	@Override
	public synchronized PutResult put(InputStream stream, PutOptions options) throws Exception {
		return withConnection(repository -> repository.put(stream, options));
	}

	@Override
	public synchronized File get(String bsn, Version version, Map<String, String> properties,
			DownloadListener... listeners) throws Exception {
		return withConnection(repository -> repository.get(bsn, version, properties, listeners));
	}

	private <T, E extends Exception> T withConnection(ThrowingFunction<P2Repository, T, E> action) throws E {
		return getSharedFramework().<T, E> withConnection(this, getLog(), repository -> {
			if (properties != null)
				repository.setProperties(properties);
			if (reporter != null)
				repository.setReporter(reporter);

			return action.apply(repository);
		});
	}

	@Override
	public boolean canWrite() {
		return withConnection(repository -> repository.canWrite());
	}

	@Override
	public List<String> list(String pattern) throws Exception {
		return withConnection(repository -> repository.list(pattern));
	}

	@Override
	public SortedSet<Version> versions(String bsn) throws Exception {
		return withConnection(repository -> repository.versions(bsn));
	}

	@Override
	public String getName() {
		return withConnection(repository -> repository.getName());
	}

	@Override
	public String getLocation() {
		return withConnection(repository -> repository.getLocation());
	}

	@Override
	public Map<Requirement, Collection<Capability>> findProviders(Collection<? extends Requirement> requirements) {
		return withConnection(repository -> repository.findProviders(requirements));
	}

	@Override
	public ResourceHandle getHandle(String bsn, String version, Strategy strategy, Map<String, String> properties)
			throws Exception {
		return withConnection(repository -> repository.getHandle(bsn, version, strategy, properties));
	}

	@Override
	public synchronized File getCacheDirectory() {
		return withConnection(repository -> repository.getCacheDirectory());
	}
}

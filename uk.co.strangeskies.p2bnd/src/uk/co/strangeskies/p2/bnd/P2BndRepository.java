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

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.jar.Manifest;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.service.repository.Repository;

import aQute.bnd.service.Plugin;
import aQute.bnd.service.RemoteRepositoryPlugin;
import aQute.bnd.service.ResourceHandle;
import aQute.bnd.service.Strategy;
import aQute.bnd.version.Version;
import aQute.service.reporter.Reporter;
import uk.co.strangeskies.p2.P2Repository;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.Log.Level;
import uk.co.strangeskies.utilities.Property;

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
public class P2BndRepository implements RemoteRepositoryPlugin, Repository, Plugin {
	static final int FRAMEWORK_TIMEOUT_SECONDS = 2;

	private static final String OSGI_CLEAN = "osgi.clean";
	private static final String OSGI_CLEAR_PERSISTED_STATE = "clearPersistedState";
	private static final String OSGI_SYSTEM_PACKAGES_EXTRA = "org.osgi.framework.system.packages.extra";

	static final Map<String, String> FRAMEWORK_PROPERTIES = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;

		{
			put(OSGI_CLEAN, Boolean.toString(true));
			put(OSGI_CLEAR_PERSISTED_STATE, Boolean.toString(true));
			put(OSGI_SYSTEM_PACKAGES_EXTRA,
					"org.osgi.framework;version=\"1.7.0\"," + "org.osgi.service.metatype;version=\"1.1.0\","
							+ "uk.co.strangeskies.utilities;version=\"1.0.0\"," + "uk.co.strangeskies.p2;version=\"1.0.0\","
							+ "aQute.bnd.service;version=\"4.1.0\"," + "aQute.bnd.version;version=\"1.3.0\","
							+ "aQute.service.reporter;version=\"1.0.0\"," + "org.osgi.service.repository;version=\"1.0.0\"");
		}
	};

	private static final String EMBEDDED_RUNPATH = "Embedded-Runpath";

	private final FrameworkLifecycleManager frameworkLifecycle;

	private Log log = (l, s) -> {};

	private P2Repository repositoryService;

	private Map<String, String> properties;
	private Reporter reporter;

	public P2BndRepository() {
		Manifest manifest = getManifest(getClass());
		String frameworkJars = manifest.getMainAttributes().getValue("Export-Package");
		System.out.println(frameworkJars);
		System.out.println(frameworkJars);
		System.out.println(frameworkJars);

		frameworkLifecycle = new FrameworkLifecycleManager(f -> {
			startService(f);

			if (properties != null)
				repositoryService.setProperties(properties);
			if (reporter != null)
				repositoryService.setReporter(reporter);
		} , FRAMEWORK_TIMEOUT_SECONDS, FRAMEWORK_PROPERTIES, getLog());
	}

	private static Manifest getManifest(Class<?> clz) {
		String resource = "/" + clz.getName().replace(".", "/") + ".class";
		String fullPath = clz.getResource(resource).toString();
		String archivePath = fullPath.substring(0, fullPath.length() - resource.length());

		/*
		 * Deal with Wars
		 */
		if (archivePath.endsWith("\\WEB-INF\\classes") || archivePath.endsWith("/WEB-INF/classes")) {
			archivePath = archivePath.substring(0, archivePath.length() - "/WEB-INF/classes".length());
		}

		try (InputStream input = new URL(archivePath + "/META-INF/MANIFEST.MF").openStream()) {
			return new Manifest(input);
		} catch (Exception e) {
			throw new RuntimeException("Loading MANIFEST for class " + clz + " failed!", e);
		}
	}

	public static void main(String[] args) {
		P2BndRepository repo = new P2BndRepository();
		System.out.println("name: " + repo.getName());
		System.out.println("name: " + repo.getName());
		System.out.println("name: " + repo.getName());

		System.out.println("great!");
	}

	private void startService(Framework framework) throws Exception {
		BundleContext frameworkContext = framework.getBundleContext();
		frameworkContext.registerService(Log.class, getLog(), new Hashtable<>());

		Property<Object, Object> repositoryService = new IdentityProperty<>();
		ServiceListener p2RepoServiceListener = event -> {
			switch (event.getType()) {
			case ServiceEvent.REGISTERED:
				ServiceReference<?> reference = event.getServiceReference();

				synchronized (repositoryService) {
					repositoryService.set(frameworkContext.getService(reference));
					repositoryService.notifyAll();
				}
			default:
			}
		};
		frameworkContext.addServiceListener(p2RepoServiceListener, "(objectclass=" + P2Repository.class.getName() + ")");

		Manifest manifest = getManifest(getClass());
		String frameworkJars = manifest.getMainAttributes().getValue(EMBEDDED_RUNPATH);

		List<Bundle> bundles = new ArrayList<>();
		Arrays.stream(frameworkJars.split(",")).map(s -> "/" + s).forEach(s -> {
			try {
				bundles.add(frameworkContext.installBundle("classpath:" + s, getClass().getResourceAsStream(s)));
			} catch (Exception e) {
				getLog().log(Level.ERROR, "Unable to add jar to internal framework " + s, e);
				throw new RuntimeException(e);
			}
		});
		for (Bundle bundle : bundles) {
			try {
				if (bundle.getHeaders().get("Fragment-Host") == null) {
					bundle.start();
				}
			} catch (Exception e) {
				getLog().log(Level.ERROR, "Unable to start bundle " + bundle, e);
				throw e;
			}
		}

		synchronized (repositoryService) {
			if (repositoryService.get() == null) {
				repositoryService.wait(2000);

				if (repositoryService.get() == null) {
					getLog().log(Level.ERROR, "Timed out waiting for P2 service");
					throw new IllegalStateException("Unable to obtain repository service");
				}
			}
		}

		this.repositoryService = (P2Repository) repositoryService.get();

		getLog().log(Level.ERROR, "Successfully wrapped P2Repository service " + repositoryService.get());
	}

	@Override
	protected void finalize() throws Throwable {
		frameworkLifecycle.stopFramework();
		super.finalize();
	}

	@Override
	public synchronized PutResult put(InputStream stream, PutOptions options) throws Exception {
		return frameworkLifecycle.withFramework(() -> repositoryService.put(stream, options));
	}

	@Override
	public synchronized File get(String bsn, Version version, Map<String, String> properties,
			DownloadListener... listeners) throws Exception {
		return frameworkLifecycle.withFramework(() -> repositoryService.get(bsn, version, properties, listeners));
	}

	@Override
	public boolean canWrite() {
		return frameworkLifecycle.withFramework(() -> repositoryService.canWrite());
	}

	@Override
	public List<String> list(String pattern) throws Exception {
		return frameworkLifecycle.withFramework(() -> repositoryService.list(pattern));
	}

	@Override
	public SortedSet<Version> versions(String bsn) throws Exception {
		return frameworkLifecycle.withFramework(() -> repositoryService.versions(bsn));
	}

	@Override
	public String getName() {
		return frameworkLifecycle.withFramework(() -> repositoryService.getName());
	}

	@Override
	public String getLocation() {
		return frameworkLifecycle.withFramework(() -> repositoryService.getLocation());
	}

	@Override
	public void setProperties(Map<String, String> map) throws Exception {
		synchronized (frameworkLifecycle) {
			properties = map;
			if (frameworkLifecycle.isStarted()) {
				repositoryService.setProperties(properties);
			}
		}
	}

	@Override
	public void setReporter(Reporter processor) {
		synchronized (frameworkLifecycle) {
			reporter = processor;

			log = new ReporterLog(reporter);
			if (frameworkLifecycle.isStarted()) {
				repositoryService.setReporter(reporter);
			}
		}
	}

	private Log getLog() {
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
					repository.log.log(level, message);
				}
			}
		};
	}

	@Override
	public Map<Requirement, Collection<Capability>> findProviders(Collection<? extends Requirement> requirements) {
		return frameworkLifecycle.withFramework(() -> repositoryService.findProviders(requirements));
	}

	@Override
	public ResourceHandle getHandle(String bsn, String version, Strategy strategy, Map<String, String> properties)
			throws Exception {
		return frameworkLifecycle.withFramework(() -> repositoryService.getHandle(bsn, version, strategy, properties));
	}

	@Override
	public synchronized File getCacheDirectory() {
		return frameworkLifecycle.withFramework(() -> repositoryService.getCacheDirectory());
	}
}

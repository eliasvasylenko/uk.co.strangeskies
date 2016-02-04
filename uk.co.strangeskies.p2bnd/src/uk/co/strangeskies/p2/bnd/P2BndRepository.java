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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.SortedSet;
import java.util.jar.Manifest;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.repository.Repository;

import aQute.bnd.service.Plugin;
import aQute.bnd.service.RemoteRepositoryPlugin;
import aQute.bnd.service.ResourceHandle;
import aQute.bnd.service.Strategy;
import aQute.bnd.version.Version;
import aQute.service.reporter.Reporter;
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
@Component(immediate = true)
public class P2BndRepository implements RemoteRepositoryPlugin, Repository, Plugin {
	private static final String EMBEDDED_RUNPATH = "Embedded-Runpath";

	private Framework framework;

	private Log log = (l, s) -> {};

	private Plugin plugin;
	private RemoteRepositoryPlugin remoteRepositoryPlugin;
	private Repository repository;

	private boolean initialised = false;

	private Map<String, String> properties;
	private Reporter reporter;

	public static Manifest getManifest(Class<?> clz) {
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

	@Activate
	public void activate() {
		initialise();
	}

	private Map<String, String> getFrameworkProperties() {
		Map<String, String> properties = new HashMap<>();

		properties.put("osgi.clean", "true");
		properties.put("clearPersistedState", "true");
		properties.put("org.osgi.framework.system.packages.extra",
				"uk.co.strangeskies.utilities;version=\"1.0.0\"," + "aQute.bnd.service;version=\"4.1.0\","
						+ "aQute.bnd.version;version=\"1.3.0\"," + "aQute.service.reporter;version=\"1.0.0\","
						+ "org.osgi.service.repository;version=\"1.0.0\"");

		return properties;
	}

	private synchronized void startService() throws Exception {
		try {
			Property<Object, Object> repositoryService = new IdentityProperty<>();

			Thread.currentThread().getContextClassLoader().loadClass("org.osgi.framework.launch.FrameworkFactory");
			FrameworkFactory frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();
			framework = frameworkFactory.newFramework(getFrameworkProperties());
			framework.start();

			BundleContext frameworkContext = framework.getBundleContext();
			frameworkContext.registerService(Log.class, (l, s) -> getLog().log(l, s), new Hashtable<>());
			frameworkContext.addServiceListener(event -> {
				switch (event.getType()) {
				case ServiceEvent.REGISTERED:
					ServiceReference<?> reference = event.getServiceReference();

					synchronized (repositoryService) {
						repositoryService.set(frameworkContext.getService(reference));
						repositoryService.notifyAll();
					}
				default:
				}
			} , "(component.name=P2RepositoryImpl)");

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
					if (bundle.getSymbolicName().equals("uk.co.strangeskies.p2.provider")) {
						System.out.println("found!");
						System.out.println("found!");
						System.out.println("found!");
						System.out.println("found!");
						System.out.println("found!");
						System.out.println("found!");
						System.out.println("found!");
						bundle.adapt(BundleWiring.class).getClassLoader().loadClass("aQute.bnd.service.RemoteRepositoryPlugin");
						bundle.adapt(BundleWiring.class).getClassLoader().loadClass("uk.co.strangeskies.p2.impl.P2RepositoryImpl")
								.newInstance();
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
						getLog().log(Level.ERROR, "Timed out waiting for P2 service as " + RemoteRepositoryPlugin.class.getName());
						throw new IllegalStateException("Unable to obtain repository service");
					}
				}
			}

			Object repositoryServiceInstance = repositoryService.get();
			/*-
			Object wrappedService = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
					new Class<?>[] { RemoteRepositoryPlugin.class, Repository.class, Plugin.class }, new InvocationHandler() {
						@Override
						public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
							return repositoryServiceInstance.getClass().getMethod(method.getName(), method.getParameterTypes())
									.invoke(repositoryServiceInstance, args);
						}
					});
			 */
			Object wrappedService = repositoryServiceInstance;
			this.remoteRepositoryPlugin = (RemoteRepositoryPlugin) wrappedService;
			this.repository = (Repository) wrappedService;
			this.plugin = (Plugin) wrappedService;

			getLog().log(Level.ERROR, "Successfully wrapped P2Repository service " + repositoryService.get());
		} catch (Exception e) {
			getLog().log(Level.ERROR, "Unable to start framework", e);
			e.printStackTrace();
			throw e;
		}
	}

	private synchronized void stopService() {
		try {
			if (initialised && framework != null) {
				framework.stop();
				framework.waitForStop(0);
			}
		} catch (Exception e) {
			getLog().log(Level.ERROR, "Unable to stop framework", e);
			throw new RuntimeException(e);
		} finally {
			initialised = false;
			framework = null;

			repository = null;
			plugin = null;
			remoteRepositoryPlugin = null;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		stopService();
		super.finalize();
	}

	@Override
	public PutResult put(InputStream stream, PutOptions options) throws Exception {
		initialise();
		return remoteRepositoryPlugin.put(stream, options);
	}

	@Override
	public File get(String bsn, Version version, Map<String, String> properties, DownloadListener... listeners)
			throws Exception {
		initialise();
		return remoteRepositoryPlugin.get(bsn, version, properties, listeners);
	}

	@Override
	public boolean canWrite() {
		initialise();
		return remoteRepositoryPlugin.canWrite();
	}

	@Override
	public List<String> list(String pattern) throws Exception {
		initialise();
		return remoteRepositoryPlugin.list(pattern);
	}

	@Override
	public SortedSet<Version> versions(String bsn) throws Exception {
		initialise();
		return remoteRepositoryPlugin.versions(bsn);
	}

	@Override
	public String getName() {
		initialise();
		return remoteRepositoryPlugin.getName();
	}

	@Override
	public String getLocation() {
		initialise();
		return remoteRepositoryPlugin.getLocation();
	}

	@Override
	public synchronized void setProperties(Map<String, String> map) throws Exception {
		properties = map;
		if (initialised) {
			plugin.setProperties(properties);
		}
	}

	@Override
	public synchronized void setReporter(Reporter processor) {
		reporter = processor;

		setLog(new ReporterLog(reporter));
		if (initialised) {
			plugin.setReporter(reporter);
		}
	}

	@Reference
	private void setLog(Log log) {
		this.log = log;
	}

	private Log getLog() {
		return log;
	}

	@Override
	public Map<Requirement, Collection<Capability>> findProviders(Collection<? extends Requirement> requirements) {
		initialise();
		return repository.findProviders(requirements);
	}

	@Override
	public ResourceHandle getHandle(String bsn, String version, Strategy strategy, Map<String, String> properties)
			throws Exception {
		initialise();
		return remoteRepositoryPlugin.getHandle(bsn, version, strategy, properties);
	}

	private synchronized void initialise() {
		if (!initialised) {
			initialised = true;
			try {
				Thread.currentThread().getContextClassLoader().loadClass("org.osgi.framework.launch.FrameworkFactory");

				startService();

				if (properties != null)
					plugin.setProperties(properties);
				if (reporter != null)
					plugin.setReporter(reporter);
			} catch (Exception e) {
				e.printStackTrace();
				stopService();
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public File getCacheDirectory() {
		initialise();
		return remoteRepositoryPlugin.getCacheDirectory();
	}
}

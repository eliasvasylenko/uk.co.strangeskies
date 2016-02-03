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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.service.component.annotations.Component;
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
	private static final String FRAMEWORK_JARS = "FrameworkJars";

	private Framework framework;

	private Log log = (l, s) -> {};

	private Plugin plugin;
	private RemoteRepositoryPlugin remoteRepositoryPlugin;
	private Repository repository;

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

	public static void main(String... args) {
		new P2BndRepository();
	}

	public P2BndRepository() {
		start();

		System.out.println(getName());
	}

	private void start() {
		try {
			Property<Object, Object> repositoryService = new IdentityProperty<>();

			FrameworkFactory frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();
			framework = frameworkFactory.newFramework(getFrameworkProperties());
			framework.start();

			BundleContext frameworkContext = framework.getBundleContext();
			frameworkContext.addServiceListener(System.out::println);
			frameworkContext.registerService(Log.class, (l, s) -> log.log(l, s), new Hashtable<>());
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
			String frameworkJars = manifest.getMainAttributes().getValue(FRAMEWORK_JARS);

			List<Bundle> bundles = new ArrayList<>();
			Arrays.stream(frameworkJars.split(",")).map(s -> "/" + s).forEach(s -> {
				try {
					bundles.add(frameworkContext.installBundle("classpath:" + s, getClass().getResourceAsStream(s)));
				} catch (RuntimeException e) {
					log.log(Level.ERROR, "Unable to add jar to internal framework " + s, e);
					throw e;
				} catch (Exception e) {
					log.log(Level.ERROR, "Unable to add jar to internal framework " + s, e);
					throw new RuntimeException(e);
				}
			});
			for (Bundle bundle : bundles) {
				try {
					if (bundle.getHeaders().get("Fragment-Host") == null) {
						bundle.start();
					}
					System.out.println("  started bundle - " + bundle.getSymbolicName());
				} catch (RuntimeException e) {
					log.log(Level.ERROR, "Unable to start bundle " + bundle, e);
					throw e;
				} catch (Exception e) {
					log.log(Level.ERROR, "Unable to start bundle " + bundle, e);
					throw new RuntimeException(e);
				}
			}

			synchronized (repositoryService) {
				if (repositoryService.get() == null) {
					repositoryService.wait(2000);

					if (repositoryService.get() == null) {
						log.log(Level.ERROR, "Timed out waiting for P2 service as " + RemoteRepositoryPlugin.class.getName());
						throw new IllegalStateException("Unable to obtain repository service");
					}
				}
			}

			Object repositoryServiceInstance = repositoryService.get();
			Object wrappedService = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
					new Class<?>[] { RemoteRepositoryPlugin.class, Plugin.class, Repository.class }, new InvocationHandler() {
						@Override
						public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
							return repositoryServiceInstance.getClass().getMethod(method.getName(), method.getParameterTypes())
									.invoke(repositoryServiceInstance, args);
						}
					});
			this.remoteRepositoryPlugin = (RemoteRepositoryPlugin) wrappedService;
			this.repository = (Repository) wrappedService;
			this.plugin = (Plugin) wrappedService;

			log.log(Level.ERROR, "Successfully wrapped P2Repository service " + repositoryService.get());
		} catch (RuntimeException e) {
			log.log(Level.ERROR, "Unable to start framework", e);
			throw e;
		} catch (Exception e) {
			log.log(Level.ERROR, "Unable to start framework", e);
			throw new RuntimeException(e);
		}
	}

	private Map<String, String> getFrameworkProperties() {
		Map<String, String> properties = new HashMap<>();

		properties.put("osgi.clean", "true");
		properties.put("clearPersistedState", "true");

		return properties;
	}

	public void stop() {
		try {
			synchronized (this) {
				if (framework != null) {
					framework.stop();
					framework.waitForStop(0);
					framework = null;
				}
			}
		} catch (Exception e) {
			log.log(Level.ERROR, "Unable to stop framework", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		stop();
		super.finalize();
	}

	@Override
	public PutResult put(InputStream stream, PutOptions options) throws Exception {
		return remoteRepositoryPlugin.put(stream, options);
	}

	@Override
	public File get(String bsn, Version version, Map<String, String> properties, DownloadListener... listeners)
			throws Exception {
		return remoteRepositoryPlugin.get(bsn, version, properties, listeners);
	}

	@Override
	public boolean canWrite() {
		return remoteRepositoryPlugin.canWrite();
	}

	@Override
	public List<String> list(String pattern) throws Exception {
		return remoteRepositoryPlugin.list(pattern);
	}

	@Override
	public SortedSet<Version> versions(String bsn) throws Exception {
		return remoteRepositoryPlugin.versions(bsn);
	}

	@Override
	public String getName() {
		return remoteRepositoryPlugin.getName();
	}

	@Override
	public String getLocation() {
		return getLocation();
	}

	@Override
	public void setProperties(Map<String, String> map) throws Exception {
		plugin.setProperties(map);
	}

	@Override
	public void setReporter(Reporter processor) {
		log = new ReporterLog(processor);
		plugin.setReporter(processor);
	}

	@Override
	public Map<Requirement, Collection<Capability>> findProviders(Collection<? extends Requirement> requirements) {
		return repository.findProviders(requirements);
	}

	@Override
	public ResourceHandle getHandle(String bsn, String version, Strategy strategy, Map<String, String> properties)
			throws Exception {
		return remoteRepositoryPlugin.getHandle(bsn, version, strategy, properties);
	}

	@Override
	public File getCacheDirectory() {
		return remoteRepositoryPlugin.getCacheDirectory();
	}
}

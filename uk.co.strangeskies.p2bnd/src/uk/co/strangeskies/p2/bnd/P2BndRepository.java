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
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
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
import uk.co.strangeskies.p2.P2Repository;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.Log.Level;
import uk.co.strangeskies.utilities.Property;

@Component(immediate = true)
public class P2BndRepository
		implements RemoteRepositoryPlugin, Repository, Plugin {
	private static final String FRAMEWORK_JARS = "FrameworkJars";

	private final Framework framework;

	private Log log = (l, s) -> {};

	private P2Repository repository;

	public static Manifest getManifest(Class<?> clz) {
		String resource = "/" + clz.getName().replace(".", "/") + ".class";
		String fullPath = clz.getResource(resource).toString();
		String archivePath = fullPath.substring(0,
				fullPath.length() - resource.length());

		/*
		 * Deal with Wars
		 */
		if (archivePath.endsWith("\\WEB-INF\\classes")
				|| archivePath.endsWith("/WEB-INF/classes")) {
			archivePath = archivePath.substring(0,
					archivePath.length() - "/WEB-INF/classes".length());
		}

		try (InputStream input = new URL(archivePath + "/META-INF/MANIFEST.MF")
				.openStream()) {
			return new Manifest(input);
		} catch (Exception e) {
			throw new RuntimeException(
					"Loading MANIFEST for class " + clz + " failed!", e);
		}
	}

	public P2BndRepository() {
		try {
			Property<P2Repository, P2Repository> repository = new IdentityProperty<>();

			FrameworkFactory frameworkFactory = ServiceLoader
					.load(FrameworkFactory.class).iterator().next();
			framework = frameworkFactory.newFramework(new HashMap<>());
			framework.start();

			BundleContext frameworkContext = framework.getBundleContext();

			ServiceListener frameworkListener = event -> {
				switch (event.getType()) {
				case ServiceEvent.REGISTERED:
					ServiceReference<?> reference = event.getServiceReference();
					synchronized (repository) {
						repository
								.set((P2Repository) frameworkContext.getService(reference));
					}
				default:
				}
			};
			frameworkContext.addServiceListener(frameworkListener,
					"(objectclass=" + P2Repository.class.getName() + ")");
			frameworkContext.registerService(Log.class, (l, s) -> log.log(l, s),
					new Hashtable<>());

			Manifest manifest = getManifest(getClass());
			String frameworkJars = manifest.getMainAttributes()
					.getValue(FRAMEWORK_JARS);

			List<Bundle> bundles = new ArrayList<>();
			Arrays.stream(frameworkJars.split(",")).map(s -> "/" + s).forEach(s -> {
				try {
					bundles.add(frameworkContext.installBundle("classpath:" + s,
							getClass().getResourceAsStream(s)));
				} catch (RuntimeException e) {
					log.log(Level.ERROR, "Unable to add jar to internal framework " + s,
							e);
					throw e;
				} catch (Exception e) {
					log.log(Level.ERROR, "Unable to add jar to internal framework " + s,
							e);
					throw new RuntimeException(e);
				}
			});
			for (Bundle bundle : bundles) {
				try {
					bundle.start();
				} catch (RuntimeException e) {
					log.log(Level.ERROR, "Unable to start bundle " + bundle, e);
					throw e;
				} catch (Exception e) {
					log.log(Level.ERROR, "Unable to start bundle " + bundle, e);
					throw new RuntimeException(e);
				}
			}

			synchronized (repository) {
				if (repository.get() == null) {
					frameworkListener.wait(2000);
					this.repository = repository.get();

					if (this.repository == null) {
						log.log(Level.ERROR, "Timed out waiting for P2 repository service");
						throw new IllegalStateException(
								"Unable to obtain repository service");
					} else {
						log.log(Level.ERROR, "Successfully wrapped P2Repository service");
					}
				}
			}
		} catch (RuntimeException e) {
			log.log(Level.ERROR, "Unable to start framework", e);
			throw e;
		} catch (Exception e) {
			log.log(Level.ERROR, "Unable to start framework", e);
			throw new RuntimeException(e);
		}
	}

	public void close() {
		try {
			framework.stop();
			framework.waitForStop(0);
		} catch (BundleException | InterruptedException e) {
			log.log(Level.ERROR, "Unable to stop framework", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	@Override
	public PutResult put(InputStream stream, PutOptions options)
			throws Exception {
		return repository.put(stream, options);
	}

	@Override
	public File get(String bsn, Version version, Map<String, String> properties,
			DownloadListener... listeners) throws Exception {
		return repository.get(bsn, version, properties, listeners);
	}

	@Override
	public boolean canWrite() {
		return repository.canWrite();
	}

	@Override
	public List<String> list(String pattern) throws Exception {
		return repository.list(pattern);
	}

	@Override
	public SortedSet<Version> versions(String bsn) throws Exception {
		return repository.versions(bsn);
	}

	@Override
	public String getName() {
		return repository.getName();
	}

	@Override
	public String getLocation() {
		return getLocation();
	}

	@Override
	public void setProperties(Map<String, String> map) throws Exception {
		repository.setProperties(map);
	}

	@Override
	public void setReporter(Reporter processor) {
		log = new ReporterLog(processor);
	}

	@Override
	public Map<Requirement, Collection<Capability>> findProviders(
			Collection<? extends Requirement> requirements) {
		return repository.findProviders(requirements);
	}

	@Override
	public ResourceHandle getHandle(String bsn, String version, Strategy strategy,
			Map<String, String> properties) throws Exception {
		return repository.getHandle(bsn, version, strategy, properties);
	}

	@Override
	public File getCacheDirectory() {
		return repository.getCacheDirectory();
	}
}

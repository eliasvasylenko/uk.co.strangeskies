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

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.jar.Manifest;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import uk.co.strangeskies.osgi.frameworkwrapper.FrameworkWrapper;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.Log.Level;
import uk.co.strangeskies.utilities.Property;
import uk.co.strangeskies.utilities.flowcontrol.Timeout;
import uk.co.strangeskies.utilities.function.ThrowingConsumer;
import uk.co.strangeskies.utilities.function.ThrowingRunnable;
import uk.co.strangeskies.utilities.function.ThrowingSupplier;

public class FrameworkWrapperImpl implements FrameworkWrapper {
	private static final String FRAGMENT_HOST_HEADER = "Fragment-Host";

	private Map<String, String> launchProperties;
	private ThrowingRunnable<?> initialisationAction;
	private ThrowingRunnable<?> shutdownAction;

	private Framework framework;

	private final Timeout timeout;
	private Log log;
	private boolean frameworkStarted = false;

	private final ClassLoader classLoader;

	private Map<String, InputStream> bundleSources = new HashMap<>();

	public FrameworkWrapperImpl() {
		this(0);
	}

	public FrameworkWrapperImpl(int timeoutMilliseconds) {
		this(timeoutMilliseconds, (l, m) -> {});
	}

	public FrameworkWrapperImpl(int timeoutMilliseconds, Log log) {
		this(timeoutMilliseconds, log, Thread.currentThread().getContextClassLoader());
	}

	public FrameworkWrapperImpl(int timeoutMilliseconds, Log log, ClassLoader classLoader) {
		this.log = log;

		timeout = new Timeout(this::stopFramework, timeoutMilliseconds, this);

		this.classLoader = classLoader;
	}

	@Override
	public void setTimeoutMilliseconds(int timeoutMilliseconds) {
		timeout.setTimeoutMilliseconds(timeoutMilliseconds);
	}

	@Override
	public void setLog(Log log) {
		this.log = log;
	}

	@Override
	public synchronized void setLaunchProperties(Map<String, String> properties) {
		launchProperties = properties;
	}

	@Override
	public synchronized void setInitialisationAction(ThrowingRunnable<?> action) {
		initialisationAction = action;
	}

	@Override
	public synchronized void setShutdownAction(ThrowingRunnable<?> action) {
		shutdownAction = action;
	}

	public synchronized void setBundles(Map<String, InputStream> bundleSources) {
		this.bundleSources.clear();
		this.bundleSources.putAll(bundleSources);
	}

	@Override
	public synchronized void startFramework() {
		if (!frameworkStarted) {
			try {
				FrameworkFactory frameworkFactory = ServiceLoader.load(FrameworkFactory.class, classLoader).iterator().next();
				framework = frameworkFactory.newFramework(launchProperties);
				framework.start();
				frameworkStarted = true;

				registerBundles();

				initialisationAction.run();
			} catch (Exception e) {
				log.log(Level.ERROR, "Unable to start framework", e);
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		timeout.set();
	}

	private void registerBundles() throws Exception {
		BundleContext frameworkContext = framework.getBundleContext();
		frameworkContext.registerService(Log.class, log, new Hashtable<>());

		List<Bundle> bundles = new ArrayList<>();
		bundleSources.entrySet().stream().forEach(b -> {
			try {
				bundles.add(frameworkContext.installBundle(b.getKey(), b.getValue()));
			} catch (Exception e) {
				log.log(Level.ERROR, "Unable to add jar to internal framework " + b.getKey(), e);
				throw new RuntimeException(e);
			}
		});
		for (Bundle bundle : bundles) {
			try {
				if (bundle.getHeaders().get(FRAGMENT_HOST_HEADER) == null) {
					bundle.start();
				}
			} catch (Exception e) {
				log.log(Level.ERROR, "Unable to start bundle " + bundle, e);
				throw e;
			}
		}

		log.log(Level.INFO, "Successfully started bundles");
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

	@Override
	public synchronized void stopFramework() {
		try {
			if (frameworkStarted) {
				framework.stop();
				framework.waitForStop(0);
				if (shutdownAction != null) {
					shutdownAction.run();
				}
			}
		} catch (Exception e) {
			log.log(Level.ERROR, "Unable to stop framework", e);
			throw new RuntimeException(e);
		} finally {
			frameworkStarted = false;
			framework = null;
		}
		notifyAll();
	}

	@Override
	public synchronized boolean isStarted() {
		return frameworkStarted;
	}

	@Override
	public synchronized Framework getFramework() {
		return framework;
	}

	@Override
	public synchronized void withFramework(ThrowingRunnable<?> action) {
		try {
			withFrameworkThrowing(action);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized <T> T withFramework(ThrowingSupplier<T, ?> action) {
		try {
			return withFrameworkThrowing(action);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized <E extends Exception> void withFrameworkThrowing(ThrowingRunnable<E> action) throws E {
		withFrameworkThrowing(() -> {
			action.run();
			return null;
		});
	}

	@Override
	public synchronized <T, E extends Exception> T withFrameworkThrowing(ThrowingSupplier<T, E> action) throws E {
		try {
			startFramework();

			T result = action.get();

			startFramework();
			return result;
		} catch (Exception e) {
			log.log(Level.ERROR, "Unable to perform framework action " + action, e);
			throw e;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		stopFramework();
		super.finalize();
	}

	@Override
	public synchronized <T> void withService(Class<T> serviceClass, Consumer<T> action) {
		try {
			withService(serviceClass, action);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized <T> void withServiceThrowing(Class<T> serviceClass, ThrowingConsumer<T, ?> action,
			int timeoutMilliseconds) throws Exception {
		BundleContext frameworkContext = framework.getBundleContext();

		Property<Object, Object> service = new IdentityProperty<>();

		String filter = "(" + Constants.OBJECTCLASS + "=" + serviceClass.getName() + ")";
		ServiceListener serviceListener = event -> {
			switch (event.getType()) {
			case ServiceEvent.REGISTERED:
				ServiceReference<?> reference = event.getServiceReference();

				synchronized (service) {
					if (service.get() == null) {
						service.set(frameworkContext.getService(reference));
					}
					service.notifyAll();
				}
			default:
			}
		};
		frameworkContext.addServiceListener(serviceListener, filter);

		ServiceReference<T> reference = frameworkContext.getServiceReference(serviceClass);
		synchronized (service) {
			if (service.get() == null && reference != null) {
				service.set(frameworkContext.getService(reference));
			}
			service.notifyAll();
		}

		synchronized (service) {
			if (service.get() == null) {
				service.wait(timeoutMilliseconds);

				if (service.get() == null) {
					log.log(Level.ERROR, "Timed out waiting for service " + serviceClass.getName());
					throw new IllegalStateException("Unable to obtain service " + serviceClass.getName());
				}
			}
		}

		action.accept((T) service.get());
	}
}

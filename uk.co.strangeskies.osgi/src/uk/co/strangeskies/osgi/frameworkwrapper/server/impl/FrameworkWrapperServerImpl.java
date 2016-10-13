/*
 * Copyright (C) 2016 ${copyright.holder.name} <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.osgi.
 *
 * uk.co.strangeskies.osgi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.osgi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.osgi.frameworkwrapper.server.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;

import uk.co.strangeskies.osgi.frameworkwrapper.FrameworkWrapper;
import uk.co.strangeskies.osgi.frameworkwrapper.server.FrameworkWrapperServer;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.Log.Level;
import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.ObservableImpl;
import uk.co.strangeskies.utilities.Property;
import uk.co.strangeskies.utilities.flowcontrol.Timeout;
import uk.co.strangeskies.utilities.function.ThrowingConsumer;
import uk.co.strangeskies.utilities.function.ThrowingFunction;
import uk.co.strangeskies.utilities.function.ThrowingRunnable;
import uk.co.strangeskies.utilities.function.ThrowingSupplier;

@SuppressWarnings("javadoc")
public class FrameworkWrapperServerImpl implements FrameworkWrapperServer {
	private static final String FRAGMENT_HOST_HEADER = "Fragment-Host";

	private Map<String, String> launchProperties;
	private ObservableImpl<FrameworkWrapper> onStart;
	private ObservableImpl<FrameworkWrapper> onStop;

	private Framework framework;

	private final Timeout timeout;
	private Log log;
	private boolean publishLogService = false;
	private boolean frameworkStarted = false;

	private FrameworkFactory frameworkFactory;

	private Map<String, ThrowingSupplier<InputStream, ?>> bundleSources = new LinkedHashMap<>();

	private ClassLoader frameworkClassLoader;

	public FrameworkWrapperServerImpl() {
		this(0);
	}

	public FrameworkWrapperServerImpl(int timeoutMilliseconds) {
		this(timeoutMilliseconds, (l, m) -> {});
	}

	public FrameworkWrapperServerImpl(int timeoutMilliseconds, Log log) {
		this(timeoutMilliseconds, log, FrameworkWrapperServerImpl.class.getClassLoader());
	}

	public FrameworkWrapperServerImpl(int timeoutMilliseconds, Log log, ClassLoader classLoader) {
		setLog(log, false);

		timeout = new Timeout(this::stopFramework, timeoutMilliseconds, this);

		setFrameworkClassLoader(classLoader);

		onStart = new ObservableImpl<>();
		onStop = new ObservableImpl<>();
	}

	@Activate
	public void activate(ComponentContext context) {
		ClassLoader usingBundleClassLoader = context.getUsingBundle().adapt(BundleWiring.class).getClassLoader();
		setFrameworkClassLoader(usingBundleClassLoader);
	}

	private synchronized void setFrameworkClassLoader(ClassLoader classLoader) {
		this.frameworkClassLoader = classLoader;
	}

	@Override
	public void setTimeoutMilliseconds(int timeoutMilliseconds) {
		timeout.setTimeoutMilliseconds(timeoutMilliseconds);
	}

	@Override
	public void setLog(Log log, boolean publishService) {
		if (log != null) {
			this.log = log;
		} else {
			this.log = (l, m) -> {};
		}
		this.publishLogService = publishService;
	}

	@Override
	public synchronized void setLaunchProperties(Map<String, String> properties) {
		launchProperties = properties;
	}

	@Override
	public synchronized Observable<FrameworkWrapper> onStart() {
		return onStart;
	}

	@Override
	public synchronized Observable<FrameworkWrapper> onStop() {
		return onStop;
	}

	@Override
	public synchronized void setBundles(Map<String, ThrowingSupplier<InputStream, ?>> bundleSources) {
		this.bundleSources.clear();
		this.bundleSources.putAll(bundleSources);
	}

	@Override
	public synchronized void startFramework() {
		timeout.stop();

		startFrameworkImpl();

		timeout.set();
	}

	private synchronized void startFrameworkImpl() {
		if (!frameworkStarted) {
			initialiseFrameworkFactory();

			try {
				log.log(Level.INFO, "Loading framework");
				framework = frameworkFactory.newFramework(launchProperties);
				log.log(Level.INFO, "Starting framework");
				framework.start();
				frameworkStarted = true;

				log.log(Level.INFO, "Registering bundles");
				registerBundles();

				onStart.fire(this);
			} catch (Exception e) {
				throw runtimeException("Unable to start framework", e);
			}
		}
	}

	private void initialiseFrameworkFactory() {
		if (frameworkFactory == null) {
			log.log(Level.INFO, "Fetching framework service loader");
			ServiceLoader<FrameworkFactory> serviceLoader = ServiceLoader.load(FrameworkFactory.class, frameworkClassLoader);

			log.log(Level.INFO, "Loading framework service");
			frameworkFactory = StreamSupport.stream(serviceLoader.spliterator(), false).findAny()
					.<RuntimeException>orElseThrow(
							() -> new RuntimeException("Cannot find service implementing " + FrameworkFactory.class.getName()));
		}
	}

	private RuntimeException runtimeException(String message, Exception e) {
		log.log(Level.ERROR, message, e);
		e.printStackTrace();
		return new RuntimeException(message, e);
	}

	private void registerBundles() throws Exception {
		BundleContext frameworkContext = framework.getBundleContext();
		if (publishLogService) {
			frameworkContext.registerService(Log.class, log, null);
		}

		List<Bundle> bundles = new ArrayList<>();

		bundleSources.entrySet().stream().forEach(b -> {
			try (InputStream input = b.getValue().get()) {
				bundles.add(frameworkContext.installBundle(b.getKey(), input));
			} catch (Exception e) {
				runtimeException("Unable to add jar to internal framework " + b.getKey(), e);
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

	@Override
	public synchronized void stopFramework() {
		try {
			if (frameworkStarted) {
				framework.stop();
				framework.waitForStop(0);
				if (onStop != null) {
					onStop.fire(this);
				}
			}
		} catch (Exception e) {
			throw runtimeException("Unable to stop framework", e);
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
	public synchronized <E extends Exception> void withFramework(ThrowingRunnable<E> action) throws E {
		withFramework(() -> {
			action.run();
			return null;
		});
	}

	@Override
	public <T, E extends Exception> T withFramework(ThrowingSupplier<? extends T, E> action) throws E {
		try {
			timeout.stop();

			synchronized (this) {
				startFrameworkImpl();

				Property<T, T> result = new IdentityProperty<>();
				result.set(action.get());

				timeout.set();
				return result.get();
			}
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
	public synchronized <T, E extends Exception> void withService(Class<T> serviceClass, String filter,
			ThrowingConsumer<? super T, E> action, int timeoutMilliseconds) throws E {
		withService(serviceClass, filter, s -> {
			action.accept(s);
			return null;
		}, timeoutMilliseconds);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, R, E extends Exception> R withService(Class<T> serviceClass, String filter,
			ThrowingFunction<? super T, ? extends R, E> action, int timeoutMilliseconds) throws E {
		try {
			BundleContext frameworkContext = framework.getBundleContext();

			Property<Object, Object> service = new IdentityProperty<>();

			if (filter == null) {
				filter = "(" + Constants.OBJECTCLASS + "=" + serviceClass.getName() + ")";
			} else {
				filter = "(&&" + filter + "(" + Constants.OBJECTCLASS + "=" + serviceClass.getName() + "))";
			}
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

			return action.apply((T) service.get());
		} catch (InterruptedException | InvalidSyntaxException e) {
			log.log(Level.ERROR, "Unexpected error performing action with service " + serviceClass, e);
			throw new RuntimeException(e);
		} catch (Exception e) {
			log.log(Level.ERROR, "Unable to perform action with service " + serviceClass, e);
			throw e;
		}
	}
}

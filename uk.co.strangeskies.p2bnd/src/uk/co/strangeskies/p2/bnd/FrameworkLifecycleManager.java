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

import java.util.Map;
import java.util.ServiceLoader;

import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.Log.Level;
import uk.co.strangeskies.utilities.function.ThrowingConsumer;
import uk.co.strangeskies.utilities.function.ThrowingRunnable;
import uk.co.strangeskies.utilities.function.ThrowingSupplier;

public class FrameworkLifecycleManager {
	private final ThrowingConsumer<Framework, ?> initialisation;
	private final int timeoutSeconds;
	private final Map<String, String> properties;
	private final Log log;

	private Framework framework;
	private boolean frameworkStarted = false;
	private boolean frameworkStopping = false;
	private Thread thread;

	public FrameworkLifecycleManager(ThrowingConsumer<Framework, ?> initialisation, int timeoutSeconds,
			Map<String, String> properties) {
		this(initialisation, timeoutSeconds, properties, (l, m) -> {});
	}

	public FrameworkLifecycleManager(ThrowingConsumer<Framework, ?> initialisation, int timeoutSeconds,
			Map<String, String> properties, Log log) {
		this.initialisation = initialisation;
		this.timeoutSeconds = timeoutSeconds;
		this.properties = properties;
		this.log = log;
	}

	private synchronized void startTimeout() {
		if (thread == null) {
			thread = new Thread(() -> {
				synchronized (this) {
					do {
						try {
							if (frameworkStarted) {
								frameworkStopping = true;
								wait(timeoutSeconds * 1000);
							}
						} catch (InterruptedException e) {}
					} while (!frameworkStopping);

					thread = null;
					stopFramework();
				}
			});
			thread.start();
		} else {
			frameworkStopping = false;
			notifyAll();
		}
	}

	public synchronized void startFramework() {
		if (!frameworkStarted) {
			try {
				FrameworkFactory frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();
				framework = frameworkFactory.newFramework(properties);
				framework.start();
				frameworkStarted = true;

				initialisation.accept(framework);
			} catch (Exception e) {
				log.log(Level.ERROR, "Unable to start framework", e);
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		startTimeout();
	}

	public synchronized void stopFramework() {
		try {
			if (frameworkStarted) {
				framework.stop();
				framework.waitForStop(0);
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

	public synchronized boolean isStarted() {
		return frameworkStarted;
	}

	public synchronized Framework getFramework() {
		return framework;
	}

	public synchronized void withFramework(ThrowingRunnable<?> action) {
		try {
			withFrameworkThrowing(action);
		} catch (Exception e) {}
	}

	public synchronized <T> T withFramework(ThrowingSupplier<T, ?> action) {
		try {
			return withFrameworkThrowing(action);
		} catch (Exception e) {
			return null;
		}
	}

	public synchronized void withFrameworkThrowing(ThrowingRunnable<?> action) throws Exception {
		withFrameworkThrowing(() -> {
			action.run();
			return null;
		});
	}

	public synchronized <T> T withFrameworkThrowing(ThrowingSupplier<T, ?> action) throws Exception {
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
}

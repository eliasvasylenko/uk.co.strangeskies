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
package uk.co.strangeskies.osgi.frameworkwrapper;

import java.io.InputStream;
import java.util.Map;
import java.util.function.Consumer;

import org.osgi.framework.launch.Framework;

import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.function.ThrowingConsumer;
import uk.co.strangeskies.utilities.function.ThrowingRunnable;
import uk.co.strangeskies.utilities.function.ThrowingSupplier;

public interface FrameworkWrapper {
	public static final String EMBEDDED_FRAMEWORK = "Embedded-Framework";
	public static final String EMBEDDED_RUNPATH = "Embedded-Runpath";

	void setTimeoutMilliseconds(int timeoutMilliseconds);

	void setLog(Log log);

	void setLaunchProperties(Map<String, String> properties);

	void setInitialisationAction(ThrowingRunnable<?> action);

	void setShutdownAction(ThrowingRunnable<?> action);

	void startFramework();

	void stopFramework();

	boolean isStarted();

	Framework getFramework();

	void withFramework(ThrowingRunnable<?> action);

	<T> T withFramework(ThrowingSupplier<T, ?> action);

	<E extends Exception> void withFrameworkThrowing(ThrowingRunnable<E> action) throws E;

	<T, E extends Exception> T withFrameworkThrowing(ThrowingSupplier<T, E> action) throws E;

	default <T> void withService(Class<T> serviceClass, Consumer<T> action, int timeoutMilliseconds) {
		withService(serviceClass, null, action, timeoutMilliseconds);
	}

	default <T> void withServiceThrowing(Class<T> serviceClass, ThrowingConsumer<T, ?> action, int timeoutMilliseconds)
			throws Exception {
		withServiceThrowing(serviceClass, null, action, timeoutMilliseconds);
	}

	default <T> void withService(Class<T> serviceClass, String filter, Consumer<T> action, int timeoutMilliseconds) {
		try {
			withServiceThrowing(serviceClass, t -> action.accept(t), timeoutMilliseconds);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	<T> void withServiceThrowing(Class<T> serviceClass, String filter, ThrowingConsumer<T, ?> action,
			int timeoutMilliseconds) throws Exception;

	void setBundles(Map<String, ThrowingSupplier<InputStream, ?>> bundleSources);
}

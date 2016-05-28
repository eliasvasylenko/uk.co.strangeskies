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

import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.function.ThrowingConsumer;
import uk.co.strangeskies.utilities.function.ThrowingFunction;
import uk.co.strangeskies.utilities.function.ThrowingRunnable;
import uk.co.strangeskies.utilities.function.ThrowingSupplier;

/**
 * A simple interface intended to provide access to some feature of an OSGi
 * framework without requiring access to any OSGi classes on the class path.
 * <p>
 * The framework is started and stopped as it is needed by the client, and may
 * optionally time-out after a period of inactivity. Methods are provided to
 * perform actions which lock on the presence of the framework such that it will
 * remain available until they are complete.
 * <p>
 * The client may access services within the framework, and register services
 * into the framework.
 * 
 * @author Elias N Vasylenko
 */
public interface FrameworkWrapper {
	/**
	 * The manifest header for specifying URLs of bundles which must be on the
	 * framework class path. Typically this will just be a framework bundle and
	 * the frameworkwrapper.server bundle.
	 */
	public static final String EMBEDDED_FRAMEWORK = "Embedded-Framework";
	/**
	 * The manifest header for specifying URLs of bundles which must be deployed
	 * into the framework.
	 */
	public static final String EMBEDDED_RUNPATH = "Embedded-Runpath";

	/**
	 * Set the duration of inactivity after which the framework should
	 * automatically shut down. This setting will be applied next time the
	 * framework timeout is reset by activity.
	 * 
	 * @param timeoutMilliseconds
	 *          the framework shutdown timeout
	 */
	void setTimeoutMilliseconds(int timeoutMilliseconds);

	/**
	 * Set the log for {@link FrameworkWrapper} events. The log object may also be
	 * published as a service into the running framework the next time it is
	 * started.
	 * 
	 * @param log
	 *          the log
	 * @param publishService
	 *          publish the log as a service component into the framework
	 */
	void setLog(Log log, boolean publishService);

	/**
	 * Set the launch properties of the framework. This setting will be applied
	 * next time the framework is started from the stopped state.
	 * 
	 * @param properties
	 *          the OSGi launch properties for the wrapped framework
	 */
	void setLaunchProperties(Map<String, String> properties);

	/**
	 * Set the bundles to be deployed in the framework. This setting will be
	 * applied next time the framework is started from the stopped state.
	 * <p>
	 * The bundles are specified in the form of a mapping from a resource
	 * identification string to an input stream supplier over the bundle jar.
	 * 
	 * @param bundleSources
	 *          a mapping from resource identification strings to input streams
	 *          from the identified resources
	 */
	void setBundles(Map<String, ThrowingSupplier<InputStream, ?>> bundleSources);

	/**
	 * Start the framework, or reset the timeout if it is already started.
	 */
	void startFramework();

	/**
	 * Stop the framework, or no action if it is already stopped.
	 */
	void stopFramework();

	/**
	 * @return an observable over framework start events
	 */
	Observable<FrameworkWrapper> onStart();

	/**
	 * @return an observable over framework stop events
	 */
	Observable<FrameworkWrapper> onStop();

	/**
	 * @return true if the framework is running, false otherwise
	 */
	boolean isStarted();

	/**
	 * Perform an action whilst guaranteeing framework availability for the
	 * action's duration.
	 * 
	 * @param <E>
	 *          the type of exceptions which may be thrown by the action
	 * @param action
	 *          an action to perform
	 * @throws E
	 *           an exception thrown by the action
	 */
	<E extends Exception> void withFramework(ThrowingRunnable<E> action) throws E;

	/**
	 * Perform a result-producing action whilst guaranteeing framework
	 * availability for the action's duration.
	 * 
	 * @param <R>
	 *          the type of the result of the action
	 * @param <E>
	 *          the type of exceptions which may be thrown by the action
	 * @param action
	 *          an action to perform
	 * @return the result of the action
	 * @throws E
	 *           an exception thrown by the action
	 */
	<R, E extends Exception> R withFramework(ThrowingSupplier<? extends R, E> action) throws E;

	/**
	 * Perform an action whilst guaranteeing framework availability for the
	 * action's duration, and making a best effort attempt to ensure service
	 * availability.
	 * 
	 * @param <T>
	 *          the type of the service
	 * @param <E>
	 *          the type of exceptions which may be thrown by the action
	 * @param serviceClass
	 *          the class of the service to fetch
	 * @param action
	 *          an action to perform
	 * @param timeoutMilliseconds
	 *          the maximum time to wait for the service to become available
	 * @throws E
	 *           an exception thrown by the action
	 */
	default <T, E extends Exception> void withService(Class<T> serviceClass, ThrowingConsumer<? super T, E> action,
			int timeoutMilliseconds) throws E {
		withService(serviceClass, null, action, timeoutMilliseconds);
	}

	/**
	 * Perform an action whilst guaranteeing framework availability for the
	 * action's duration, and making a best effort attempt to ensure service
	 * availability.
	 * <p>
	 * The client may provide a service filter as per the OSGi specification. The
	 * filter will automatically be combined with a filter for "objectClass" to
	 * match the given service class. The "objectClass" alone will be used if the
	 * given filter is null.
	 * 
	 * @param <T>
	 *          the type of the service
	 * @param <E>
	 *          the type of exceptions which may be thrown by the action
	 * @param serviceClass
	 *          the class of the service to fetch
	 * @param filter
	 *          the OSGi service filter to match against the service, or null
	 * @param action
	 *          an action to perform
	 * @param timeoutMilliseconds
	 *          the maximum time to wait for the service to become available
	 * @throws E
	 *           an exception thrown by the action
	 */
	<T, E extends Exception> void withService(Class<T> serviceClass, String filter, ThrowingConsumer<? super T, E> action,
			int timeoutMilliseconds) throws E;

	/**
	 * Perform a result-producing action whilst guaranteeing framework
	 * availability for the action's duration, and making a best effort attempt to
	 * ensure service availability.
	 * 
	 * @param <T>
	 *          the type of the service
	 * @param <R>
	 *          the type of the result of the action
	 * @param <E>
	 *          the type of exceptions which may be thrown by the action
	 * @param serviceClass
	 *          the class of the service to fetch
	 * @param action
	 *          an action to perform
	 * @param timeoutMilliseconds
	 *          the maximum time to wait for the service to become available
	 * @return the result of the action
	 * @throws E
	 *           an exception thrown by the action
	 */
	default <T, R, E extends Exception> R withService(Class<T> serviceClass,
			ThrowingFunction<? super T, ? extends R, E> action, int timeoutMilliseconds) throws E {
		return withService(serviceClass, null, action, timeoutMilliseconds);
	}

	/**
	 * Perform a result-producing action whilst guaranteeing framework
	 * availability for the action's duration, and making a best effort attempt to
	 * ensure service availability.
	 * <p>
	 * The client may provide a service filter as per the OSGi specification. The
	 * filter will automatically be combined with a filter for "objectClass" to
	 * match the given service class. The "objectClass" alone will be used if the
	 * given filter is null.
	 * 
	 * @param <T>
	 *          the type of the service
	 * @param <R>
	 *          the type of the result of the action
	 * @param <E>
	 *          the type of exceptions which may be thrown by the action
	 * @param serviceClass
	 *          the class of the service to fetch
	 * @param filter
	 *          the OSGi service filter to match against the service, or null
	 * @param action
	 *          an action to perform
	 * @param timeoutMilliseconds
	 *          the maximum time to wait for the service to become available
	 * @return the result of the action
	 * @throws E
	 *           an exception thrown by the action
	 */
	<T, R, E extends Exception> R withService(Class<T> serviceClass, String filter,
			ThrowingFunction<? super T, ? extends R, E> action, int timeoutMilliseconds) throws E;
}

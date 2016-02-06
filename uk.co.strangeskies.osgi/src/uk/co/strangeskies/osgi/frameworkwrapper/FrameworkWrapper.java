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

	<T> void withService(Class<T> serviceClass, Consumer<T> action);

	<T> void withServiceThrowing(Class<T> serviceClass, ThrowingConsumer<T, ?> action, int timeoutMilliseconds)
			throws Exception;

	void setBundles(Map<String, InputStream> bundleSources);
}

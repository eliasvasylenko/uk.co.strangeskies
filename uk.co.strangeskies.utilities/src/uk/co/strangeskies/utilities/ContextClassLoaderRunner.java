package uk.co.strangeskies.utilities;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

/**
 * Utilities for safely running code under a different thread context class
 * loader.
 * 
 * @author Elias N Vasylenko
 */
public class ContextClassLoaderRunner {
	private final ClassLoader classLoader;

	public ContextClassLoaderRunner(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public ContextClassLoaderRunner(URL... jars) {
		this(new URLClassLoader(jars,
				Thread.currentThread().getContextClassLoader()));
	}

	public ContextClassLoaderRunner(Collection<URL> jars) {
		this(jars.toArray(new URL[jars.size()]));
	}

	/**
	 * Invoke a {@link Runnable} under this runners classloader, making sure the
	 * current context class loader is reinstated upon termination.
	 * 
	 * @param runnable
	 *          The runnable to be invoked under the given classloader
	 */
	public void run(Runnable runnable) {
		ClassLoader originalClassLoader = Thread.currentThread()
				.getContextClassLoader();
		Thread.currentThread().setContextClassLoader(classLoader);

		try {
			runnable.run();
		} finally {
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}
	}
}

/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.classloading;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.function.Supplier;

import uk.co.strangeskies.utilities.function.ThrowingRunnable;
import uk.co.strangeskies.utilities.function.ThrowingSupplier;

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
		this(new URLClassLoader(jars, Thread.currentThread().getContextClassLoader()));
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
	public <T, E extends Exception> T runThrowing(ThrowingSupplier<T, E> runnable) throws E {
		ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(classLoader);

		try {
			return runnable.get();
		} finally {
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}
	}

	/**
	 * Invoke a {@link Runnable} under this runners classloader, making sure the
	 * current context class loader is reinstated upon termination.
	 * 
	 * @param runnable
	 *          The runnable to be invoked under the given classloader
	 */
	public <T> T run(Supplier<T> runnable) {
		return runThrowing(runnable::get);
	}

	public void run(Runnable runnable) {
		runThrowing(runnable::run);
	}

	public <E extends Exception> void runThrowing(ThrowingRunnable<E> runnable) throws E {
		runThrowing(() -> {
			runnable.run();
			return null;
		});
	}

	public void runLater(Runnable runnable) {
		new Thread(() -> run(runnable)).start();
	}
}

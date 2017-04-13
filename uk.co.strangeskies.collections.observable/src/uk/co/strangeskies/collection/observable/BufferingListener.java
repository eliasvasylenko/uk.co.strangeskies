/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
 * This file is part of uk.co.strangeskies.utility.
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
package uk.co.strangeskies.collection.observable;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.function.Function.identity;

import java.util.concurrent.Executor;

/**
 * An implementation of {@link ForwardingListener} which simply pipes event
 * objects to chained listeners asynchronously with the original event thread.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          The type of event to listen for
 */
public class BufferingListener<T> extends MappingListener<T, T> {
	/**
	 * As @see {@link #BufferingListener(Executor)} with a sequential, single
	 * thread executor.
	 */
	public BufferingListener() {
		this(newSingleThreadExecutor());
	}

	/**
	 * Initialize a buffering listener with an empty set of listeners. Events will
	 * be forwarded from the given executor.
	 * 
	 * @param executor
	 *          the executor through which to issue forwarded events
	 */
	public BufferingListener(Executor executor) {
		super(identity(), executor);
	}
}

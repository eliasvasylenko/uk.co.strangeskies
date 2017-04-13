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

import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * An implementation of {@link ForwardingListener} which pipes the latest
 * available event objects to listeners as quickly as they can keep up with
 * production.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of event to listen for
 * @param <R>
 *          the type of the event to emit
 */
public class MappingListener<T, R> extends ForwardingListener<T, R> {
	private final Executor executor;
	private final Function<T, R> mapping;

	/**
	 * Initialize a mapping listener with an empty set of listeners.
	 * 
	 * @param mapping
	 *          the mapping to perform for forwarded events
	 * @param executor
	 *          the executor to issue forwarded events
	 */
	public MappingListener(Function<T, R> mapping, Executor executor) {
		this.executor = executor;
		this.mapping = mapping;
	}

	/**
	 * As @see {@link #MappingListener(Function, Executor)} with an executor which
	 * invokes immediately on the calling thread.
	 */
	@SuppressWarnings("javadoc")
	public MappingListener(Function<T, R> mapping) {
		this(mapping, r -> r.run());
	}

	@Override
	public void notify(T item) {
		executor.execute(() -> fire(mapping.apply(item)));
	}
}

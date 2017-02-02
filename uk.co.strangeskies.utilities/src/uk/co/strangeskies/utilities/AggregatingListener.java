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
package uk.co.strangeskies.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import uk.co.strangeskies.utilities.flowcontrol.SerialExecutor;

/**
 * An implementation of {@link ForwardingListener} which aggregates consumed
 * data into a list, then provides it to listeners in chunks of all remaining
 * data.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          The type of event to listen for
 */
public class AggregatingListener<T> extends ForwardingListener<T, List<T>> {
	public AggregatingListener(Executor executor) {
		this(new SerialExecutor(executor), new ArrayList<>());
	}

	private AggregatingListener(SerialExecutor executor, List<T> list) {
		super(fire -> item -> executor.execute(() -> fire.accept(item)));
	}

	public AggregatingListener() {
		super(new Buffer<T, List<T>>() {
			private final  = new ArrayList<>();

			@Override
			public boolean isReady() {
				return !list.isEmpty();
			}

			@Override
			public List<T> get() {
				List<T> aggregate = new ArrayList<>(list);
				list.clear();
				return aggregate;
			}

			@Override
			public void put(T item) {
				list.add(item);
			}
		});
	}
}

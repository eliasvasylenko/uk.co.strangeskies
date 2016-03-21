/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.utilities.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities;

import java.util.ArrayList;
import java.util.List;

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
	/**
	 * Initialise a buffering listener with an empty queue and an empty set of
	 * listeners.
	 */
	public AggregatingListener() {
		super(new Buffer<T, List<T>>() {
			private final List<T> list = new ArrayList<>();

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
/**
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.
 *
 *     uk.co.strangeskies is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     uk.co.strangeskies is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with uk.co.strangeskies.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.collection.computingmap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class ComputingEntryHashMap<K, V> implements ComputingMap<K, V> {
	protected interface Entry<K, V> {
		K getKey();

		V getValue();

		void remove();
	}

	private final Map<K, Entry<K, V>> map;
	private final Function<K, V> computation;

	public ComputingEntryHashMap(Function<K, V> computation) {
		map = new HashMap<>();
		this.computation = computation;
	}

	protected ComputingEntryHashMap(ComputingEntryHashMap<K, V> other) {
		map = other.map;
		computation = other.computation;
	}

	@Override
	public V get(K key) {
		Entry<?, V> entry = map.get(key);

		return entry == null ? null : entry.getValue();
	}

	@Override
	public boolean put(K key) {
		if (map.containsKey(key))
			return false;

		Entry<K, V> entry = createEntry(key);
		map.put(key, entry);

		return true;
	}

	@Override
	public V putGet(K key) {
		V value = get(key);

		if (value == null) {
			put(key);
			value = get(key);
		}

		return value;
	}

	protected Entry<K, V> createEntry(K key) {
		V value = computation.apply(key);

		return new Entry<K, V>() {
			@Override
			public K getKey() {
				return key;
			}

			@Override
			public V getValue() {
				return value;
			}

			@Override
			public void remove() {}
		};
	}

	protected Function<K, V> computation() {
		return computation;
	}

	@Override
	public Set<K> keySet() {
		return Collections.unmodifiableSet(map.keySet());
	}

	@Override
	public boolean remove(K key) {
		return map.remove(key) != null;
	}

	@Override
	public boolean clear() {
		if (!map.isEmpty())
			return false;
		map.clear();
		return true;
	}
}

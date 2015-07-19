/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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

	protected class DeferredEntry implements Entry<K, V> {
		private final K key;
		private V value;

		public DeferredEntry(K key) {
			this.key = key;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public synchronized V getValue() {
			return value != null ? value : (value = computation().apply(getKey()));
		}

		@Override
		public void remove() {}
	}

	protected class ComputingEntry extends DeferredEntry {
		public ComputingEntry(K key) {
			super(key);
			new Thread(this::getValue).start();
		}
	}

	private final Map<K, Entry<K, V>> map;
	private final Function<K, V> computation;

	protected ComputingEntryHashMap(Function<K, V> computation) {
		this.map = new HashMap<>();
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

	public V putGetImpl(K key) {
		Entry<K, V> entry = map.get(key);

		if (entry == null) {
			entry = createEntry(key);
			map.put(key, entry);
		}

		return entry.getValue();
	}

	@Override
	public V putGet(K key) {
		V value = get(key);

		if (value == null) {
			value = putGetImpl(key);
		}

		return value;
	}

	protected Entry<K, V> createEntry(K key) {
		return new ComputingEntry(key);
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

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

import java.util.AbstractSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FutureMap<K, V> implements ComputingMap<K, V> {
	private final HashMap<K, Thread> preparationThreads;
	private final Consumer<K> prepare;
	private final Function<K, V> mapping;

	public FutureMap(Consumer<K> prepare, Function<K, V> mapping) {
		preparationThreads = new HashMap<>();
		this.prepare = prepare;
		this.mapping = mapping;
	}

	public FutureMap(Function<K, V> function) {
		this(function, HashMap::new);
	}

	public FutureMap(Function<K, V> function, Supplier<Map<K, V>> valueMap) {
		preparationThreads = new HashMap<>();

		Map<K, V> values = valueMap.get();
		prepare = key -> {
			V value = function.apply(key);

			synchronized (preparationThreads) {
				if (preparationThreads.containsKey(key))
					values.put(key, value);
			}
		};
		mapping = values::get;
	}

	public boolean put(final K key) {
		synchronized (preparationThreads) {
			if (preparationThreads.containsKey(key))
				return false;

			Thread thread = new Thread(() -> prepare.accept(key));
			thread.start();
			preparationThreads.put(key, thread);

			return true;
		}
	}

	public V get(K key) {
		synchronized (preparationThreads) {
			if (!preparationThreads.containsKey(key))
				return null;
		}

		wait(key);

		synchronized (preparationThreads) {
			if (!preparationThreads.containsKey(key))
				return null;
			return mapping.apply(key);
		}
	}

	public V putGet(K key) {
		V value = get(key);
		while (value == null) {
			put(key);
			value = get(key);
		}
		return value;
	}

	@Override
	public Set<K> keySet() {
		return new AbstractSet<K>() {
			Set<K> baseSet = preparationThreads.keySet();

			@Override
			public Iterator<K> iterator() {
				Iterator<K> baseIterator = baseSet.iterator();
				return new Iterator<K>() {
					private K last;

					@Override
					public boolean hasNext() {
						return baseIterator.hasNext();
					}

					@Override
					public K next() {
						return last = baseIterator.next();
					}

					public void remove() {
						FutureMap.this.wait(last);
						baseIterator.remove();
					}
				};
			}

			@Override
			public int size() {
				return baseSet.size();
			}
		};
	}

	@Override
	public boolean remove(K key) {
		synchronized (preparationThreads) {
			if (!preparationThreads.containsKey(key))
				return false;

			preparationThreads.remove(key).interrupt();

			return true;
		}
	}

	private boolean wait(K key) {
		Thread thread;
		synchronized (preparationThreads) {
			thread = preparationThreads.get(key);
		}

		if (thread == null)
			return false;

		while (true) {
			try {
				thread.join();
				return true;
			} catch (InterruptedException e) {
			}
		}
	}

	public boolean clear() {
		synchronized (preparationThreads) {
			if (preparationThreads.isEmpty())
				return false;
		}

		waitForAll();

		synchronized (preparationThreads) {
			preparationThreads.clear();
			return true;
		}
	}

	public void waitForAll() {
		Set<K> done = new HashSet<>();
		Set<K> remaining;

		boolean finished;
		do {
			synchronized (preparationThreads) {
				remaining = new HashSet<>(preparationThreads.keySet());
			}

			for (K key : remaining)
				if (done.add(key))
					wait(key);

			synchronized (preparationThreads) {
				finished = done.containsAll(preparationThreads.keySet());
			}
		} while (!finished);
	}

	public synchronized Set<K> getKeys() {
		return preparationThreads.keySet();
	}
}

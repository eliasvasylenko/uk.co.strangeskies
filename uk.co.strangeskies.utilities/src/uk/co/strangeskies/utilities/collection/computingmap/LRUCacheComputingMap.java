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

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import uk.co.strangeskies.utilities.IdentityProperty;

public class LRUCacheComputingMap<K, V> extends CacheComputingMap<K, V> {
	protected class LinkedEntry extends KeyedReference {
		private LinkedEntry previous;
		private LinkedEntry next;

		public LinkedEntry() {
			previous = this;
			next = this;
		}

		public LinkedEntry(K key) {
			super(key);

			previous = bounds.previous;
			next = bounds;

			previous.next = bounds.previous = this;

			size.set(size.get() + 1);
		}

		@Override
		public V getValue() {
			previous.next = next;
			next.previous = previous;

			next = bounds.next;
			previous = bounds;

			previous.next = this;
			next.previous = this;

			return super.getValue();
		}

		@Override
		public void remove() {
			previous.next = next;
			next.previous = previous;

			size.set(size.get() - 1);
		}
	}

	private final IdentityProperty<Integer> size;
	private final int maximumSize;
	private final LinkedEntry bounds;

	public LRUCacheComputingMap(Function<K, V> computation, int maximumSize,
			boolean softReferences) {
		super(computation, softReferences);

		size = new IdentityProperty<>(0);
		this.maximumSize = maximumSize;
		this.bounds = new LinkedEntry();
	}

	protected LRUCacheComputingMap(LRUCacheComputingMap<K, V> other) {
		super(other);

		size = other.size;
		maximumSize = other.maximumSize;
		bounds = other.bounds;
	}

	@Override
	protected Entry<K, V> createEntry(K key) {
		return new LinkedEntry(key);
	}

	public int cacheSize() {
		return maximumSize;
	}

	@Override
	public V get(K key) {
		return super.get(key);
	}

	@Override
	public boolean put(K key) {
		boolean added = super.put(key);

		if (size.get() > maximumSize)
			remove(bounds.previous.getKey());

		return added;
	}

	public boolean putAll(Collection<? extends K> keys) {
		boolean changed = false;
		for (K key : keys)
			changed = super.put(key) | changed;

		while (size.get() > maximumSize)
			remove(bounds.previous.getKey());

		return changed;
	}

	@Override
	public V putGet(K key) {
		V value = super.putGet(key);

		if (size.get() > maximumSize)
			remove(bounds.previous.getKey());

		return value;
	}

	@Override
	public boolean remove(K key) {
		super.get(key);
		return super.remove(key);
	}

	public boolean removeAll(Set<K> keys) {
		boolean changed = false;
		for (K key : keys)
			changed = remove(key) | changed;
		return changed;
	}

	@Override
	public boolean clear() {
		bounds.previous = bounds.next = bounds;
		return super.clear();
	}

	@Override
	public boolean isEmpty() {
		return bounds.previous == bounds;
	}
}

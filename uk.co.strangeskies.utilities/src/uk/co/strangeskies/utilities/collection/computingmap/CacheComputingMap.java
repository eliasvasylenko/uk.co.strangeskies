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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.function.Function;

public class CacheComputingMap<K, V> extends ComputingEntryHashMap<K, V> {
	protected class KeyedReference extends SoftReference<V> implements
			Entry<K, V> {
		private final K key;
		@SuppressWarnings("unused")
		private final V value;

		protected KeyedReference() {
			super(null);
			key = null;
			value = null;
		}

		public KeyedReference(K key) {
			super(computation().apply(key), references);
			this.key = key;
			this.value = softReferences ? null : get();
		}

		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return get();
		}

		@Override
		public void remove() {
		}
	}

	private final ReferenceQueue<V> references;
	private final boolean softReferences;

	public CacheComputingMap(Function<K, V> computation, boolean softReferences) {
		super(computation);
		references = new ReferenceQueue<>();
		this.softReferences = softReferences;
	}

	protected CacheComputingMap(CacheComputingMap<K, V> other) {
		super(other);
		references = other.references;
		softReferences = other.softReferences;
	}

	@SuppressWarnings("unchecked")
	public void clean() {
		KeyedReference oldReference;
		while ((oldReference = (KeyedReference) references.poll()) != null)
			remove(oldReference.key);
	}

	@Override
	protected ComputingEntryHashMap.Entry<K, V> createEntry(K key) {
		return new KeyedReference(key);
	}

	@Override
	public V get(K key) {
		clean();

		return super.get(key);
	}

	@Override
	public boolean put(K key) {
		clean();

		return super.put(key);
	}

	@Override
	public V putGet(K key) {
		clean();

		return super.putGet(key);
	}

	@Override
	public boolean remove(K key) {
		clean();

		return super.remove(key);
	}
}
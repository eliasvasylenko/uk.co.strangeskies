package uk.co.strangeskies.utilities.collection;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Set;
import java.util.function.Function;

public class CacheComputingMap<K, V> extends ComputingHashMap<K, V> {
	protected class KeyedReference extends SoftReference<V> implements
			Entry<K, V> {
		private final K key;

		public KeyedReference(K key, V value) {
			super(value, references);
			this.key = key;
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

	public CacheComputingMap(Function<K, V> computation) {
		super(computation);
		references = new ReferenceQueue<>();
	}

	protected CacheComputingMap(CacheComputingMap<K, V> other) {
		super(other);
		references = other.references;
	}

	@SuppressWarnings("unchecked")
	public void clean() {
		KeyedReference oldReference;
		while ((oldReference = (KeyedReference) references.poll()) != null)
			remove(oldReference.key);
	}

	@Override
	protected ComputingHashMap.Entry<K, V> createEntry(K key, V value) {
		return new KeyedReference(key, value);
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
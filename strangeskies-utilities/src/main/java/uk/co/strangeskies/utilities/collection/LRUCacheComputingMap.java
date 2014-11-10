package uk.co.strangeskies.utilities.collection;

import java.util.Set;
import java.util.function.Function;

import uk.co.strangeskies.utilities.IdentityProperty;

public class LRUCacheComputingMap<K, V> extends CacheComputingMap<K, V> {
	protected class LinkedEntry extends KeyedReference {
		private LinkedEntry previous = null;
		private LinkedEntry next = null;

		public LinkedEntry() {
			super(null, null);

			previous = this;
			next = this;
		}

		public LinkedEntry(K key, V value) {
			super(key, value);

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

	public LRUCacheComputingMap(Function<K, V> computation, int maximumSize) {
		super(computation);

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
	protected Entry<K, V> createEntry(K key, V value) {
		return new LinkedEntry(key, value);
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

	public boolean putAll(Set<K> keys) {
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

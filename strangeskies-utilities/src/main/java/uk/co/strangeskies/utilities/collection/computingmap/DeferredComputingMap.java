package uk.co.strangeskies.utilities.collection.computingmap;

import java.util.function.Function;

public class DeferredComputingMap<K, V> extends ComputingEntryHashMap<K, V> {
	protected class DeferredEntry implements Entry<K, V> {
		private final K key;
		private V value;

		public DeferredEntry(K key) {
			this.key = key;
			this.value = null;
		}

		public K getKey() {
			return key;
		}

		@Override
		public synchronized V getValue() {
			return value != null ? value : (value = computation().apply(getKey()));
		}

		@Override
		public void remove() {
		}
	}

	public DeferredComputingMap(Function<K, V> computation) {
		super(computation);
	}

	@Override
	protected Entry<K, V> createEntry(K key) {
		return new DeferredEntry(key);
	}
}

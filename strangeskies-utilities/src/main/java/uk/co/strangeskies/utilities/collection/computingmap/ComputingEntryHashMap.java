package uk.co.strangeskies.utilities.collection.computingmap;

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
			public void remove() {
			}
		};
	}

	protected Function<K, V> computation() {
		return computation;
	}

	@Override
	public Set<K> keySet() {
		return map.keySet();
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
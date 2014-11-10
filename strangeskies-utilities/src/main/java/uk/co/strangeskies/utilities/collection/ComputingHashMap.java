package uk.co.strangeskies.utilities.collection;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class ComputingHashMap<K, V> implements ComputingMap<K, V> {
	protected interface Entry<K, V> {
		K getKey();

		V getValue();

		void remove();
	}

	private final Map<K, Entry<K, V>> map;
	private final Function<K, V> computation;

	public ComputingHashMap(Function<K, V> computation) {
		map = new HashMap<>();
		this.computation = computation;
	}

	protected ComputingHashMap(ComputingHashMap<K, V> other) {
		map = other.map;
		computation = other.computation;
	}

	@Override
	public V get(K key) {
		return map.get(key).getValue();
	}

	@Override
	public boolean put(K key) {
		if (map.containsKey(key))
			return false;
		return putGet(key) != null;
	}

	@Override
	public V putGet(K key) {
		V value = computation.apply(key);

		if (value != null)
			map.put(key, createEntry(key, value));

		return value;
	}

	protected Entry<K, V> createEntry(K key, V value) {
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
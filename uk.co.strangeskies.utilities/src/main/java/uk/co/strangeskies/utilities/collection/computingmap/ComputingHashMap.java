package uk.co.strangeskies.utilities.collection.computingmap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class ComputingHashMap<K, V> implements ComputingMap<K, V> {
	private final Map<K, V> map;
	private final Function<K, V> computation;

	public ComputingHashMap(Function<K, V> computation) {
		map = new HashMap<>();
		this.computation = computation;
	}

	@Override
	public V get(K key) {
		return map.get(key);
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
			map.put(key, value);
		return value;
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

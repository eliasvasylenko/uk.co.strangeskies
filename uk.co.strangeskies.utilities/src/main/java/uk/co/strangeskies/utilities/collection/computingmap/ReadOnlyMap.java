package uk.co.strangeskies.utilities.collection.computingmap;

import java.util.Set;

public interface ReadOnlyMap<K, V> {
	V get(K key);

	Set<K> keySet();

	default boolean isEmpty() {
		return keySet().isEmpty();
	}
}

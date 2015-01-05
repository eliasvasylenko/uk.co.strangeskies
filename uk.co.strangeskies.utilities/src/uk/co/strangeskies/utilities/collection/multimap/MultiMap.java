package uk.co.strangeskies.utilities.collection.multimap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public interface MultiMap<K, V, C extends Collection<V>> extends Map<K, C> {
	public C createCollection();

	public default C getCollection(K key) {
		C values = get(key);

		if (values == null) {
			values = createCollection();
			put(key, values);
		}

		return values;
	}

	public default boolean add(K key, V value) {
		return getCollection(key).add(value);
	}

	public default boolean addAll(K key, Collection<? extends V> values) {
		return getCollection(key).addAll(values);
	}

	public default boolean addAll(Map<? extends K, ? extends V> values) {
		boolean added = false;

		for (Map.Entry<? extends K, ? extends V> entry : values.entrySet()) {
			added = add(entry.getKey(), entry.getValue()) || added;
		}

		return added;
	}

	public default boolean addAll(MultiMap<? extends K, ? extends V, ?> values) {
		boolean added = false;

		for (Map.Entry<? extends K, ? extends Collection<? extends V>> entry : values
				.entrySet()) {
			added = addAll(entry.getKey(), entry.getValue()) || added;
		}

		return added;
	}

	public default boolean addAll(K key,
			@SuppressWarnings("unchecked") V... values) {
		return addAll(key, Arrays.asList(values));
	}

	public default boolean addToAll(V value) {
		return addToAll(keySet(), value);
	}

	public default boolean addAllToAll(Collection<? extends V> values) {
		return addAllToAll(keySet(), values);
	}

	public default boolean addAllToAll(@SuppressWarnings("unchecked") V... values) {
		return addAllToAll(Arrays.asList(values));
	}

	public default boolean addToAll(Collection<? extends K> keys, V value) {
		boolean added = false;

		for (K key : keys) {
			added = add(key, value) || added;
		}

		return added;
	}

	public default boolean addAllToAll(Collection<? extends K> keys,
			Collection<? extends V> values) {
		boolean added = false;

		for (K key : keys) {
			added = addAll(key, values) || added;
		}

		return added;
	}

	public default boolean removeValue(K key, V value) {
		C values = get(key);

		boolean removed = values != null && values.remove(value);

		if (removed && values.isEmpty()) {
			remove(key);
		}

		return removed;
	}

	public default boolean removeAll(K key, Collection<? extends V> values) {
		C currentValues = get(key);

		boolean removed = currentValues != null && currentValues.removeAll(values);

		if (removed && currentValues.isEmpty()) {
			remove(key);
		}

		return removed;
	}

	public default boolean removeAll(K key,
			@SuppressWarnings("unchecked") V... values) {
		return addAll(key, Arrays.asList(values));
	}

	public default boolean removeAll(Map<K, V> values) {
		boolean removed = false;

		for (Map.Entry<K, V> entry : values.entrySet()) {
			removed = remove(entry.getKey(), entry.getValue()) || removed;
		}

		return removed;
	}

	public default boolean removeAll(MultiMap<K, V, ?> values) {
		boolean removed = false;

		for (Map.Entry<K, ? extends Collection<V>> entry : values.entrySet()) {
			removed = removeAll(entry.getKey(), entry.getValue()) || removed;
		}

		return removed;
	}

	public default boolean removeFromAll(V value) {
		return removeFromAll(keySet(), value);
	}

	public default boolean removeAllFromAll(Collection<? extends V> values) {
		return removeAllFromAll(keySet(), values);
	}

	public default boolean removeAllFromAll(
			@SuppressWarnings("unchecked") V... values) {
		return removeAllFromAll(Arrays.asList(values));
	}

	public default boolean removeFromAll(Collection<? extends K> keys, V value) {
		boolean removed = false;

		for (K key : keys) {
			removed = removeValue(key, value) || removed;
		}

		return removed;
	}

	public default boolean removeAllFromAll(Collection<? extends K> keys,
			Collection<? extends V> values) {
		boolean removed = false;

		for (K key : keys) {
			removed = removeAll(key, values) || removed;
		}

		return removed;
	}

	public default boolean contains(K key, V value) {
		C values = get(key);
		return values != null && values.contains(value);
	}

	public default C getAll() {
		C allValues = createCollection();

		for (C values : values()) {
			allValues.addAll(values);
		}

		return allValues;
	}

	public default C getAll(Collection<? extends K> keys) {
		C allValues = createCollection();

		for (K key : keys) {
			allValues.addAll(get(key));
		}

		return allValues;
	}

	public default C getAll(@SuppressWarnings("unchecked") K... keys) {
		return getAll(Arrays.asList(keys));
	}
}

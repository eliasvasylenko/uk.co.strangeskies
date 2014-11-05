package uk.co.strangeskies.utilities.collection;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import uk.co.strangeskies.utilities.IdentityComparator.IDReference;

public class ComputingCache<K, V> implements ComputingMap<K, V> {
	private final Map<K, WeakReference<V>> map;
	private final Function<K, V> computation;
	private ReferenceQueue<V> references;

	public ComputingCache(Function<K, V> computation) {
		map = new HashMap<>();
		this.computation = computation;
		references = new ReferenceQueue<>();
	}

	public void clean() {
		WeakReference<?> oldReference;
		while ((oldReference = (WeakReference<?>) references.poll()) != null) {
			List<IDReference<T>> collisions = collisionMap.get(oldReference.getId());

			if (collisions.size() > 1) {
				collisions.remove(oldReference);
			} else {
				collisionMap.remove(collisions);
			}
		}
	}

	@Override
	public V get(K key) {
		return map.get(key).get();
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
			map.put(key, new WeakReference<>(value, references));

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
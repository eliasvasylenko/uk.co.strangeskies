package uk.co.strangeskies.gears.utilities.flowcontrol;

import java.util.HashMap;

public class StoredFutureMap<K, V> extends FutureMap<K, V> {
	public StoredFutureMap(final Mapping<K, V> mapping) {
		super(new FutureMap.Mapping<K, V>() {
			private final HashMap<K, V> results = new HashMap<>();

			@Override
			public void prepare(K key) {
				results.put(key, mapping.prepare(key));
			}

			@Override
			public V get(K key) {
				return results.get(key);
			}
		});
	}

	public interface Mapping<K, V> {
		public V prepare(K key);
	}
}

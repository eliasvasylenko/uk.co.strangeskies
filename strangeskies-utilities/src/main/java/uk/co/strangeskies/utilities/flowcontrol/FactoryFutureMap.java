package uk.co.strangeskies.utilities.flowcontrol;

import uk.co.strangeskies.utilities.factory.Factory;

public class FactoryFutureMap<K extends Factory<? extends V>, V> extends
		StoredFutureMap<K, V> {
	public FactoryFutureMap() {
		super(new Mapping<K, V>() {
			@Override
			public V prepare(K key) {
				return key.create();
			}
		});
	}
}

package uk.co.strangeskies.gears.utilities.flowcontrol;

import uk.co.strangeskies.gears.utilities.factory.Factory;

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

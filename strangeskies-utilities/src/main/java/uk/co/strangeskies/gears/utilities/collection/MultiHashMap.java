package uk.co.strangeskies.gears.utilities.collection;

import java.util.Collection;
import java.util.HashMap;

import uk.co.strangeskies.gears.utilities.factory.Factory;

public class MultiHashMap<K, V, C extends Collection<V>> extends HashMap<K, C>
		implements MultiMap<K, V, C> {
	private static final long serialVersionUID = 1L;

	private final Factory<C> collectionFactory;

	public MultiHashMap(Factory<C> collectionFactory) {
		this.collectionFactory = collectionFactory;
	}

	@Override
	public C createCollection() {
		return collectionFactory.create();
	}
}

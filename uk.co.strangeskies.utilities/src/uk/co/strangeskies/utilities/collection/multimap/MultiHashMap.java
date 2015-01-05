package uk.co.strangeskies.utilities.collection.multimap;

import java.util.Collection;
import java.util.HashMap;

import uk.co.strangeskies.utilities.factory.Factory;

public class MultiHashMap<K, V, C extends Collection<V>> extends HashMap<K, C>
		implements MultiMap<K, V, C> {
	private static final long serialVersionUID = 1L;

	private final Factory<C> collectionFactory;

	public MultiHashMap(Factory<C> collectionFactory) {
		this.collectionFactory = collectionFactory;
	}

	public MultiHashMap(MultiMap<? extends K, ? extends V, ? extends C> that) {
		// TODO this will leak memory by retaining references to other map...
		this(that::createCollection);

		addAll(that);
	}

	@Override
	public C createCollection() {
		return collectionFactory.create();
	}
}

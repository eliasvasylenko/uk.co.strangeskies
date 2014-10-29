package uk.co.strangeskies.utilities.collection;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeMap;

import uk.co.strangeskies.utilities.factory.Factory;

public class MultiTreeMap<K, V, C extends Collection<V>> extends TreeMap<K, C>
		implements MultiMap<K, V, C> {
	private static final long serialVersionUID = 1L;

	private final Factory<C> collectionFactory;

	public MultiTreeMap(Factory<C> collectionFactory) {
		this.collectionFactory = collectionFactory;
	}

	public MultiTreeMap(Comparator<K> comparator, Factory<C> collectionFactory) {
		super(comparator);
		this.collectionFactory = collectionFactory;
	}

	@Override
	public C createCollection() {
		return collectionFactory.create();
	}
}

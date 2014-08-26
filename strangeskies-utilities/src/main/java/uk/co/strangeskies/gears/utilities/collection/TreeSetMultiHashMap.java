package uk.co.strangeskies.gears.utilities.collection;

import java.util.Set;
import java.util.TreeSet;

import uk.co.strangeskies.gears.utilities.factory.Factory;

public class TreeSetMultiHashMap<K, V> extends MultiHashMap<K, V, Set<V>>
		implements SetMultiMap<K, V> {
	private static final long serialVersionUID = 1L;

	public TreeSetMultiHashMap() {
		super(new Factory<Set<V>>() {
			@Override
			public Set<V> create() {
				return new TreeSet<V>();
			}
		});
	}
}

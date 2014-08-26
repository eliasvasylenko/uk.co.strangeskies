package uk.co.strangeskies.gears.utilities.collection;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MergedCollectionSet<T> extends AbstractSet<T> {
	private final Set<? extends Collection<? extends T>> backingCollections;

	public MergedCollectionSet(
			Set<? extends Collection<? extends T>> backingCollections) {
		this.backingCollections = backingCollections;
	}

	@Override
	public Iterator<T> iterator() {
		return getSnapshot().iterator();
	}

	@Override
	public final int size() {
		return getSnapshot().size();
	}

	public Set<T> getSnapshot() {
		HashSet<T> set = new HashSet<>();

		for (Collection<? extends T> collection : backingCollections) {
			set.addAll(collection);
		}

		return set;
	}
}

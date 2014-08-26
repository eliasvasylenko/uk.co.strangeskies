package uk.co.strangeskies.gears.utilities.function.collection;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Function;

/**
 * A view of a list which will be automatically updated along with the original,
 * but who's elements will be a transformation of the original associated
 * elements by way of the function passed to the constructor. The implementation
 * employs lazy evaluation, so try to use get() as little as possible by reusing
 * the result.
 * 
 * @author Elias N Vasylenko
 * 
 * @param <T>
 *          The type of the elements of this list.
 * @param <F>
 *          The type of the elements of the backing list.
 */
public class SetTransformationView<F, T> extends AbstractSet<T> {
	private final Collection<? extends F> backingCollection;
	private final Function<? super F, ? extends T> function;

	public SetTransformationView(Collection<? extends F> backingCollection,
			Function<? super F, ? extends T> function) {
		this.backingCollection = backingCollection;
		this.function = function;
	}

	public final Collection<F> getBackingCollection() {
		return Collections.unmodifiableCollection(backingCollection);
	}

	protected final Collection<? extends F> getModifiablebackingCollection() {
		return backingCollection;
	}

	@Override
	public final int size() {
		return backingCollection.size();
	}

	@Override
	public Iterator<T> iterator() {
		final Iterator<? extends F> backingIterator = backingCollection.iterator();
		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return backingIterator.hasNext();
			}

			@Override
			public T next() {
				return function.apply(backingIterator.next());
			}

			@Override
			public void remove() {
				backingIterator.remove();
			}
		};
	}
}

package uk.co.strangeskies.gears.utilities.function.collection;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;
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
public class ListTransformationView<F, T> extends AbstractList<T> {
	private final List<? extends F> backingList;
	private final Function<? super F, ? extends T> function;

	public ListTransformationView(List<? extends F> backingList,
			Function<? super F, ? extends T> function) {
		this.backingList = backingList;
		this.function = function;
	}

	@Override
	public final T get(int index) {
		return function.apply(backingList.get(index));
	}

	public final List<F> getBackingList() {
		return Collections.unmodifiableList(backingList);
	}

	protected final List<? extends F> getModifiableBackingList() {
		return backingList;
	}

	@Override
	public final int size() {
		return backingList.size();
	}
}

package uk.co.strangeskies.gears.mathematics.expression.collection;

import java.util.AbstractList;
import java.util.List;

import uk.co.strangeskies.gears.mathematics.expression.Expression;

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
public class ListExpressionView<T> extends AbstractList<T> {
	private final Expression<? extends List<? extends T>> backingList;

	public ListExpressionView(Expression<? extends List<? extends T>> backingList) {
		this.backingList = backingList;
	}

	@Override
	public final T get(int index) {
		return backingList.getValue().get(index);
	}

	@Override
	public final int size() {
		return backingList.getValue().size();
	}
}

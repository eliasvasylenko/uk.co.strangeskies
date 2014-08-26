package uk.co.strangeskies.gears.mathematics.expression.collection;

import java.util.AbstractList;
import java.util.List;
import java.util.function.Function;

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
public class ListExpressionTransformationView<F, T> extends AbstractList<T> {
	private final Expression<? extends List<? extends F>> backingList;
	private final Expression<? extends Function<? super F, ? extends T>> function;

	public ListExpressionTransformationView(
			Expression<? extends List<? extends F>> backingList,
			Expression<? extends Function<? super F, ? extends T>> function) {
		this.backingList = backingList;
		this.function = function;
	}

	@Override
	public final T get(int index) {
		return function.getValue().apply(backingList.getValue().get(index));
	}

	public final List<F> getBackingList() {
		return new ListExpressionView<F>(backingList);
	}

	public final Expression<? extends Function<? super F, ? extends T>> getFunction() {
		return function;
	}

	@Override
	public final int size() {
		return backingList.getValue().size();
	}
}

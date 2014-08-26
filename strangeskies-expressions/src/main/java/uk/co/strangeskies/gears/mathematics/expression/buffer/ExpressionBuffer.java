package uk.co.strangeskies.gears.mathematics.expression.buffer;

import java.util.function.BiFunction;
import java.util.function.Function;

import uk.co.strangeskies.gears.mathematics.expression.Expression;
import uk.co.strangeskies.gears.utilities.Observer;

public class ExpressionBuffer<F extends Expression<?>, T> extends
		FunctionBuffer<F, T> {
	private Observer<Expression<?>> backObserver;

	public ExpressionBuffer(T front, F back,
			BiFunction<? super T, ? super F, ? extends T> operation) {
		super(front, back, operation);
	}

	public ExpressionBuffer(F back, Function<? super F, ? extends T> function) {
		super(back, function);
	}

	public ExpressionBuffer(T front, F back,
			Function<? super F, ? extends T> function) {
		super(front, back, function);
	}

	public ExpressionBuffer(FunctionBuffer<F, T> doubleBuffer) {
		super(doubleBuffer);
	}

	@Override
	public F setBack(F next) {
		if (getBack() != null) {
			getBack().removeObserver(getBackObserver());
		}

		if (next != null) {
			next.addObserver(nextBackObserver());
		}

		return super.setBack(next);
	}

	private Observer<Expression<?>> nextBackObserver() {
		return backObserver = m -> invalidateBack();
	}

	private Observer<Expression<?>> getBackObserver() {
		return backObserver;
	}
}

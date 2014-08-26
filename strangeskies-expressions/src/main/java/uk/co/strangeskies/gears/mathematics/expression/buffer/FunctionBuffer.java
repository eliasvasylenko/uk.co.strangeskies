package uk.co.strangeskies.gears.mathematics.expression.buffer;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import uk.co.strangeskies.gears.mathematics.expression.Expression;
import uk.co.strangeskies.gears.mathematics.expression.IdentityExpression;
import uk.co.strangeskies.gears.utilities.function.TransformationOperation;

public class FunctionBuffer<B, F> extends IdentityExpression<F> implements
		DoubleBuffer<B, F> {
	private IdentityExpression<B> back;

	private boolean isFlat;

	private final BiFunction<? super F, ? super B, ? extends F> operation;

	public FunctionBuffer(F front, B back,
			BiFunction<? super F, ? super B, ? extends F> operation) {
		setFront(front);
		setBack(back);

		this.operation = operation;
	}

	public FunctionBuffer(F front, B back,
			Function<? super B, ? extends F> function) {
		this(front, back, new TransformationOperation<F, B>(function));
	}

	public FunctionBuffer(B back, Function<? super B, ? extends F> function) {
		this(function.apply(back), back,
				new TransformationOperation<F, B>(function));
	}

	public FunctionBuffer(FunctionBuffer<B, F> doubleBuffer) {
		this(doubleBuffer.getFront(), doubleBuffer.getBack(), doubleBuffer
				.getOperation());
	}

	@Override
	public Expression<B> getBackExpression() {
		return back;
	}

	public BiFunction<? super F, ? super B, ? extends F> getOperation() {
		return operation;
	}

	@Override
	public F setFront(F front) {
		isFlat = false;

		return set(front);
	}

	@Override
	public B setBack(B back) {
		isFlat = false;

		return this.back.set(back);
	}

	@Override
	public F getFront() {
		return get();
	}

	@Override
	public B getBack() {
		return back.get();
	}

	@Override
	public void push() {
		if (!isFlat) {
			setFront(getOperation().apply(getFront(), getBack()));
			isFlat = true;
		}
	}

	@Override
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}

		if (!(that instanceof FunctionBuffer<?, ?>)) {
			return false;
		}

		DoubleBuffer<?, ?> thatDoubleBuffer = (DoubleBuffer<?, ?>) that;

		F thisFront = this.getFront();
		B thisBack = this.getBack();
		Object thatFront = thatDoubleBuffer.getFront();
		Object thatBack = thatDoubleBuffer.getBack();

		return Objects.equals(thisFront, thatFront)
				&& Objects.equals(thisBack, thatBack);
	}

	@Override
	public int hashCode() {
		int hashCode = getFront().hashCode() + getBack().hashCode() * 29;

		return hashCode;
	}

	@Override
	public boolean isFlat() {
		return isFlat;
	}

	@Override
	public String toString() {
		return "[" + getBack() + "] => [" + getFront() + "]";
	}
}

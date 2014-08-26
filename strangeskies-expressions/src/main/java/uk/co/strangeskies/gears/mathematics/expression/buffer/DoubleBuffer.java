package uk.co.strangeskies.gears.mathematics.expression.buffer;

import uk.co.strangeskies.gears.mathematics.expression.Expression;

public interface DoubleBuffer<B, F> extends Expression<F> {
	public abstract F setFront(F front);

	public abstract B setBack(B back);

	public default void set(B value) {
		setBack(value);
		push();
	}

	public default void set(DoubleBuffer<? extends B, ? extends F> value) {
		setFront(value.getFront());
		setBack(value.getBack());
	}

	public abstract F getFront();

	public abstract B getBack();

	public abstract Expression<B> getBackExpression();

	public abstract void push();

	public abstract boolean isFlat();

	public default void invalidateBack() {
		setBack(getBack());
	}
}
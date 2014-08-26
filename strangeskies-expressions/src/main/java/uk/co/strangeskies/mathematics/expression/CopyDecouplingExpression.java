package uk.co.strangeskies.mathematics.expression;

import uk.co.strangeskies.utilities.Copyable;

public interface CopyDecouplingExpression<T extends Copyable<T>> extends
		Expression<T> {
	@Override
	public default T decoupleValue() {
		return getValue().copy();
	}
}

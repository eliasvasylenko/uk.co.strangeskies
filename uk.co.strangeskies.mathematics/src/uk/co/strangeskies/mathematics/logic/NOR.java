package uk.co.strangeskies.mathematics.logic;

import uk.co.strangeskies.mathematics.expression.BiFunctionExpression;
import uk.co.strangeskies.mathematics.expression.Expression;

public class NOR<O extends NORable<?, ? super T>, T>
		extends
		BiFunctionExpression</*  */NORable<? extends O, ? super T>, /*
																																							 * @
																																							 * ReadOnly
																																							 */T, /*
																																										 * @
																																										 * ReadOnly
																																										 */O> {
	public NOR(
			Expression<? extends /*  */NORable<? extends O, ? super T>> firstOperand,
			Expression<? extends /*  */T> secondOperand) {
		super(firstOperand, secondOperand, new NOROperation<O, T>());
	}
}

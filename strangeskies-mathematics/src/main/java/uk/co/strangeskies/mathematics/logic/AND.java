package uk.co.strangeskies.mathematics.logic;

import uk.co.strangeskies.mathematics.expression.BiFunctionExpression;
import uk.co.strangeskies.mathematics.expression.Expression;

public class AND<O extends ANDable<?, ? super T>, T>
		extends
		BiFunctionExpression< /* @ReadOnly */ANDable<? extends O, ? super T>, /*
																																								 * @
																																								 * ReadOnly
																																								 */T, /*
																																											 * @
																																											 * ReadOnly
																																											 */O> {
	public AND(
			Expression<? extends /* @ReadOnly */ANDable<? extends O, ? super T>> firstOperand,
			Expression<? extends /* @ReadOnly */T> secondOperand) {
		super(firstOperand, secondOperand, new ANDOperation<O, T>());
	}
}

package uk.co.strangeskies.gears.mathematics.logic;

import uk.co.strangeskies.gears.mathematics.expression.BiFunctionExpression;
import uk.co.strangeskies.gears.mathematics.expression.Expression;

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

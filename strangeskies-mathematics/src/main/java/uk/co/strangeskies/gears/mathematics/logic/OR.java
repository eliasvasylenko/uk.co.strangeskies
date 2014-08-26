package uk.co.strangeskies.gears.mathematics.logic;

import uk.co.strangeskies.gears.mathematics.expression.BiFunctionExpression;
import uk.co.strangeskies.gears.mathematics.expression.Expression;

public class OR<O extends ORable<?, ? super T>, T>
		extends
		BiFunctionExpression< /* @ReadOnly */ORable<? extends O, ? super T>, /*
																																								 * @
																																								 * ReadOnly
																																								 */T, /*
																																											 * @
																																											 * ReadOnly
																																											 */O> {
	public OR(
			Expression<? extends /* @ReadOnly */ORable<? extends O, ? super T>> firstOperand,
			Expression<? extends /* @ReadOnly */T> secondOperand) {
		super(firstOperand, secondOperand, new OROperation<O, T>());
	}
}

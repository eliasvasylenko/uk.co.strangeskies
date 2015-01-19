package uk.co.strangeskies.mathematics.logic;

import uk.co.strangeskies.mathematics.expression.BiFunctionExpression;
import uk.co.strangeskies.mathematics.expression.Expression;

public class XOR<O extends XORable<?, ? super T>, T>
		extends
		BiFunctionExpression</*  */XORable<? extends O, ? super T>, /*
																																							 * @
																																							 * ReadOnly
																																							 */T, /*
																																										 * @
																																										 * ReadOnly
																																										 */O> {
	public XOR(
			Expression<? extends /*  */XORable<? extends O, ? super T>> firstOperand,
			Expression<? extends /*  */T> secondOperand) {
		super(firstOperand, secondOperand, new XOROperation<O, T>());
	}
}

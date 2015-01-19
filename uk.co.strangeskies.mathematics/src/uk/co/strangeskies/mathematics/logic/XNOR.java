package uk.co.strangeskies.mathematics.logic;

import uk.co.strangeskies.mathematics.expression.BiFunctionExpression;
import uk.co.strangeskies.mathematics.expression.Expression;

public class XNOR<O extends XNORable<?, ? super T>, T>
		extends
		BiFunctionExpression</*  */XNORable<? extends O, ? super T>, /*
																																								 * @
																																								 * ReadOnly
																																								 */T, /*
																																											 * @
																																											 * ReadOnly
																																											 */O> {
	public XNOR(
			Expression<? extends /*  */XNORable<? extends O, ? super T>> firstOperand,
			Expression<? extends /*  */T> secondOperand) {
		super(firstOperand, secondOperand, new XNOROperation<O, T>());
	}
}

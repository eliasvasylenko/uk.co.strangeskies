package uk.co.strangeskies.mathematics.logic;

import uk.co.strangeskies.mathematics.expression.BiFunctionExpression;
import uk.co.strangeskies.mathematics.expression.Expression;

public class NAND<O extends NANDable<?, ? super T>, T>
		extends
		BiFunctionExpression</*  */NANDable<? extends O, ? super T>, /*
																																								 * @
																																								 * ReadOnly
																																								 */T, /*
																																											 * @
																																											 * ReadOnly
																																											 */O> {
	public NAND(
			Expression<? extends /*  */NANDable<? extends O, ? super T>> firstOperand,
			Expression<? extends /*  */T> secondOperand) {
		super(firstOperand, secondOperand, new NANDOperation<O, T>());
	}
}

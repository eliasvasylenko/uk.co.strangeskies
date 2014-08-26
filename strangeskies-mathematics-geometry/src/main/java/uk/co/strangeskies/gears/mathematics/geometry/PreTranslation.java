package uk.co.strangeskies.gears.mathematics.geometry;

import uk.co.strangeskies.gears.mathematics.expression.BiFunctionExpression;
import uk.co.strangeskies.gears.mathematics.expression.Expression;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector;

public class PreTranslation<O>
		extends
		BiFunctionExpression<NonCommutativelyTranslatable<? extends O>, Vector<?, ?>, O> {
	public PreTranslation(
			Expression<? extends NonCommutativelyTranslatable<? extends O>> firstOperand,
			Expression<? extends Vector<?, ?>> secondOperand) {
		super(firstOperand, secondOperand, new PreTranslate<O>());
	}

	@Override
	public String toString() {
		return "(" + getFirstOperand() + " * " + getSecondOperand() + ")";
	}
}

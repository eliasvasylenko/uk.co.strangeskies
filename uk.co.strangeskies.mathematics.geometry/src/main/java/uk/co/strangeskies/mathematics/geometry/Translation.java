package uk.co.strangeskies.mathematics.geometry;

import uk.co.strangeskies.mathematics.expression.BiFunctionExpression;
import uk.co.strangeskies.mathematics.expression.Expression;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector;

public class Translation<O> extends
		BiFunctionExpression<Translatable<? extends O>, Vector<?, ?>, O> {
	public Translation(
			Expression<? extends Translatable<? extends O>> firstOperand,
			Expression<? extends Vector<?, ?>> secondOperand) {
		super(firstOperand, secondOperand, (a, b) -> a.getTranslated(b));
	}

	@Override
	public String toString() {
		return "(" + getFirstOperand() + " * " + getSecondOperand() + ")";
	}
}

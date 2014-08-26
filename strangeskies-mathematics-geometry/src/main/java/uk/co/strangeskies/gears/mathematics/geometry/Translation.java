package uk.co.strangeskies.gears.mathematics.geometry;

import uk.co.strangeskies.gears.mathematics.expression.BiFunctionExpression;
import uk.co.strangeskies.gears.mathematics.expression.Expression;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector;

public class Translation<O> extends
		BiFunctionExpression<Translatable<? extends O>, Vector<?, ?>, O> {
	public Translation(
			Expression<? extends Translatable<? extends O>> firstOperand,
			Expression<? extends Vector<?, ?>> secondOperand) {
		super(firstOperand, secondOperand, new Translate<O>());
	}

	@Override
	public String toString() {
		return "(" + getFirstOperand() + " * " + getSecondOperand() + ")";
	}
}

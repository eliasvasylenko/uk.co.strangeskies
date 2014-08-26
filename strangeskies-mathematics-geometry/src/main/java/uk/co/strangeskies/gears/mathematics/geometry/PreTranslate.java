package uk.co.strangeskies.gears.mathematics.geometry;

import java.util.function.BiFunction;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector;

public class PreTranslate<O> implements
		BiFunction<NonCommutativelyTranslatable<? extends O>, Vector<?, ?>, O> {
	@Override
	public O apply(NonCommutativelyTranslatable<? extends O> firstOperand,
			Vector<?, ?> secondOperand) {
		return firstOperand.getPreTranslated(secondOperand);
	}
}

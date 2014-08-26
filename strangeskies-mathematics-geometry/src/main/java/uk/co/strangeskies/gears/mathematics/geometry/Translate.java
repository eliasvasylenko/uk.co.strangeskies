package uk.co.strangeskies.gears.mathematics.geometry;

import java.util.function.BiFunction;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector;

public class Translate<O> implements
		BiFunction<Translatable<? extends O>, Vector<?, ?>, O> {
	@Override
	public O apply(Translatable<? extends O> firstOperand,
			Vector<?, ?> secondOperand) {
		return firstOperand.getTranslated(secondOperand);
	}
}

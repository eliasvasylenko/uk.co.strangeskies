package uk.co.strangeskies.mathematics;

import java.util.function.Function;

public class Negate<O> implements Function<Negatable<?, ? extends O>, O> {
	@Override
	public O apply(Negatable<?, ? extends O> firstOperand) {
		return firstOperand.getNegated();
	}
}

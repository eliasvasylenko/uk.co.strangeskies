package uk.co.strangeskies.gears.utilities.function;

import uk.co.strangeskies.gears.utilities.Decorator;

public class InvertibleFunctionComposition<T, R> extends
		Decorator<TransparentInvertibleFunctionComposition<T, ?, R>> implements
		InvertibleFunction<T, R> {
	private InvertibleFunctionComposition<R, T> inverse;

	public <I> InvertibleFunctionComposition(
			InvertibleFunction<T, I> firstFunction,
			InvertibleFunction<I, R> secondFunction) {
		super(new TransparentInvertibleFunctionComposition<>(firstFunction,
				secondFunction));
	}

	protected InvertibleFunctionComposition(
			InvertibleFunctionComposition<R, T> inverse) {
		super(inverse.getComponent().getInverse());

		this.inverse = inverse;
	}

	@Override
	public R apply(T input) {
		return getComponent().apply(input);
	}

	@Override
	public InvertibleFunctionComposition<R, T> getInverse() {
		return inverse;
	}
}

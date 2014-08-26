package uk.co.strangeskies.gears.utilities.function;

import java.util.function.Function;

public class TransparentInvertibleFunctionComposition<T, I, R> implements
		Function<T, R>, InvertibleFunction<T, R> {
	private InvertibleFunction<T, I> firstFunction;
	private InvertibleFunction<I, R> secondFunction;
	private I intermediateResult;

	private TransparentInvertibleFunctionComposition<R, I, T> inverse;

	public TransparentInvertibleFunctionComposition(
			InvertibleFunction<T, I> firstFunction,
			InvertibleFunction<I, R> secondFunction) {
		this.firstFunction = firstFunction;
		this.secondFunction = secondFunction;

		inverse = new TransparentInvertibleFunctionComposition<R, I, T>(this);
	}

	protected TransparentInvertibleFunctionComposition(
			TransparentInvertibleFunctionComposition<R, I, T> inverse) {
		this.inverse = inverse;

		firstFunction = inverse.getSecondFunction().getInverse();
		secondFunction = inverse.getFirstFunction().getInverse();
	}

	@Override
	public R apply(T input) {
		intermediateResult = firstFunction.apply(input);
		return secondFunction.apply(intermediateResult);
	}

	public I getIntermediateResult() {
		return intermediateResult;
	}

	public InvertibleFunction<T, I> getFirstFunction() {
		return firstFunction;
	}

	public InvertibleFunction<I, R> getSecondFunction() {
		return secondFunction;
	}

	@Override
	public TransparentInvertibleFunctionComposition<R, I, T> getInverse() {
		return inverse;
	}
}

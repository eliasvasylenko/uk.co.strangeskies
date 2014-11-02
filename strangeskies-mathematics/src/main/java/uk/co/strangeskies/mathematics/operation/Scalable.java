package uk.co.strangeskies.mathematics.operation;

import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.Self;

public interface Scalable<S extends Scalable<S>> extends Self<S> {
	public S multiply(Value<?> value);

	public default S multiply(int value) {
		return multiply((long) value);
	}

	public S multiply(long value);

	public default S multiply(float value) {
		return multiply((double) value);
	}

	public S multiply(double value);

	public S divide(Value<?> value);

	public default S divide(int value) {
		return divide((long) value);
	}

	public S divide(long value);

	public default S divide(float value) {
		return divide((double) value);
	}

	public S divide(double value);

	public default S getMultiplied(Value<?> value) {
		return copy().multiply(value);
	}

	public default S getMultiplied(int value) {
		return copy().multiply(value);
	}

	public default S getMultiplied(long value) {
		return copy().multiply(value);
	}

	public default S getMultiplied(float value) {
		return copy().multiply(value);
	}

	public default S getMultiplied(double value) {
		return copy().multiply(value);
	}

	public default S getDivided(Value<?> value) {
		return copy().divide(value);
	}

	public default S getDivided(int value) {
		return copy().divide(value);
	}

	public default S getDivided(long value) {
		return copy().divide(value);
	}

	public default S getDivided(float value) {
		return copy().divide(value);
	}

	public default S getDivided(double value) {
		return copy().divide(value);
	}
}

package uk.co.strangeskies.reflection;

public interface VariableResult<T> extends ValueResult<T> {
	void set(T value);
}

package uk.co.strangeskies.reflection;

public interface Locals {
	<T> T get(VariableExpression<T> variableResult);

	<T> void set(VariableExpression<T> variableResult, T value);
}

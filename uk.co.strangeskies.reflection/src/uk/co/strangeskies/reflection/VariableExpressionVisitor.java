package uk.co.strangeskies.reflection;

public interface VariableExpressionVisitor<T, U> {
	<V extends U, O> T visitField(ValueExpression<? extends O> value, FieldMember<O, V> field);
}

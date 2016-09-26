package uk.co.strangeskies.reflection;

import java.util.List;

public interface ValueExpressionVisitor<T, U> {
	<V extends U> Vis<? extends T, V> visitValue(TypeToken<V> type);

	interface Vis<T, U> {
		<V extends U> T visitVariable(VariableExpression<V> variable);

		<V extends U> T visitAssignment(VariableExpression<V> target, ValueExpression<? extends V> value);

		<V extends U> T visitLiteral(V value, TypeToken<V> type);

		T visitNull();

		<V extends U> T visitReceiver(ClassDefinition<V> classDefinition);

		<V extends U, O> T visitMethod(ValueExpression<O> receiver, InvocableMember<? super O, V> invocable,
				List<ValueExpression<?>> arguments);
	}
}

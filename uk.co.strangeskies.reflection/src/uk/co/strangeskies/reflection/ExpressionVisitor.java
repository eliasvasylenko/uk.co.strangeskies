package uk.co.strangeskies.reflection;

import java.util.List;

public interface ExpressionVisitor {
	<U> ValueExpressionVisitor<U> value(TypeToken<U> type);

	interface ValueExpressionVisitor<U> {
		VariableExpressionVisitor<U> variable();

		void visitAssignment(VariableExpression<U> target, ValueExpression<? extends U> value);

		void visitLiteral(U value);

		void visitNull();

		void visitReceiver(ClassDefinition<U> classDefinition);

		<O> void visitMethod(ValueExpression<O> receiver, InvocableMember<? super O, U> invocable,
				List<ValueExpression<?>> arguments);

		void visitLocal(LocalVariable<? extends U> local);
	}

	interface VariableExpressionVisitor<U> {
		<O> void visitField(ValueExpression<? extends O> value, FieldMember<O, U> field);

		void visitLocal(LocalVariable<U> local);
	}
}

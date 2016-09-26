package uk.co.strangeskies.reflection;

public interface StatementVisitor {
	void visitReturn();

	<T> void visitReturn(ValueExpression<T> expression);

	void visitExpression(Expression expression);

	<T> void visitDeclaration(LocalVariableExpression<T> variable);

	<T> void visitDeclaration(LocalValueExpression<T> variable, ValueExpression<? extends T> initializer);
}

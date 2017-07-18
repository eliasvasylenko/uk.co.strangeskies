package uk.co.strangeskies.mathematics.expression;

import uk.co.strangeskies.observable.Disposable;

public interface ExpressionDependency<T> extends Disposable {
	Expression<T> getExpression();
}

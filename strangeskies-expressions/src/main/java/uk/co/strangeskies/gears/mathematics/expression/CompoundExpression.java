package uk.co.strangeskies.gears.mathematics.expression;

import java.util.Arrays;
import java.util.Collection;

import uk.co.strangeskies.gears.mathematics.expression.collection.ExpressionTreeSet;
import uk.co.strangeskies.gears.mathematics.expression.collection.SortedExpressionSet;
import uk.co.strangeskies.gears.utilities.IdentityComparator;
import uk.co.strangeskies.gears.utilities.Observer;

public abstract class CompoundExpression<T> extends MutableExpression<T> {
	private final SortedExpressionSet<?, Expression<? extends Object>> dependencies;

	private T value;

	private boolean evaluated = false;

	public CompoundExpression(Collection<? extends Expression<?>> dependencies) {
		this();

		this.dependencies.addAll(dependencies);
	}

	public CompoundExpression(Expression<?>... dependencies) {
		this();

		this.dependencies.addAll(Arrays.asList(dependencies));
	}

	public CompoundExpression() {
		dependencies = new ExpressionTreeSet<>(new IdentityComparator<>());

		dependencies.addObserver(new Observer<Expression<? extends Object>>() {
			@Override
			public void notify(Expression<? extends Object> message) {
				update();
			}
		});
	}

	protected final void update() {
		if (evaluated) {
			evaluated = false;
			postUpdate();
		}
	}

	@Override
	public final T getValue() {
		if (!evaluated) {
			value = evaluate();
			evaluated = true;
		}
		return value;
	}

	public final boolean isEvaluated() {
		return evaluated;
	}

	protected final T getCurrentValue() {
		return value;
	}

	protected abstract T evaluate();

	protected SortedExpressionSet<?, Expression<?>> getDependencies() {
		return dependencies;
	}
}

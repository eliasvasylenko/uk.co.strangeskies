package uk.co.strangeskies.mathematics.expression;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import uk.co.strangeskies.mathematics.expression.collection.ExpressionTreeSet;
import uk.co.strangeskies.mathematics.expression.collection.SortedExpressionSet;
import uk.co.strangeskies.utilities.IdentityComparator;

public abstract class CompoundExpression<T> extends MutableExpression<T> {
	private final SortedExpressionSet<?, Expression<? extends Object>> dependencies;

	private T value;

	private boolean evaluated;
	private final boolean parallel;

	public CompoundExpression(Collection<? extends Expression<?>> dependencies) {
		this();

		this.dependencies.addAll(dependencies);
	}

	public CompoundExpression(Collection<? extends Expression<?>> dependencies,
			boolean parallel) {
		this(parallel);

		this.dependencies.addAll(dependencies);
	}

	public CompoundExpression(Expression<?>... dependencies) {
		this(true);

		this.dependencies.addAll(Arrays.asList(dependencies));
	}

	public CompoundExpression(boolean parallel) {
		dependencies = new ExpressionTreeSet<>(new IdentityComparator<>());
		dependencies.addObserver(m -> update());

		evaluated = false;
		this.parallel = parallel;
	}

	protected final void update() {
		synchronized (dependencies) {
			if (evaluated) {
				getLock().writeLock().lock();

				evaluated = false;
				postUpdate();

				getLock().writeLock().unlock();
			}
		}
	}

	@Override
	public final T getValue() {
		Set<Expression<?>> dependencies = getDependencies();

		synchronized (dependencies) {
			if (!evaluated) {
				getLock().readLock().lock();
				if (parallel)
					for (Expression<?> dependency : dependencies) {
						dependency.getLock().readLock().lock();
						new Thread(() -> dependency.getValue()).run();
					}

				value = evaluate();
				evaluated = true;

				if (parallel)
					for (Expression<?> dependency : dependencies)
						dependency.getLock().readLock().unlock();
				getLock().readLock().unlock();
			}
		}

		return value;
	}

	public final boolean isEvaluated() {
		return evaluated;
	}

	protected abstract T evaluate();

	protected SortedExpressionSet<?, Expression<?>> getDependencies() {
		return dependencies;
	}
}

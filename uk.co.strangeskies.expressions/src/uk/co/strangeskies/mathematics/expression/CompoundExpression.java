/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.expressions.
 *
 * uk.co.strangeskies.expressions is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.expressions is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.expressions.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.expression;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import uk.co.strangeskies.mathematics.expression.collection.ExpressionTreeSet;
import uk.co.strangeskies.mathematics.expression.collection.SortedExpressionSet;
import uk.co.strangeskies.utilities.IdentityComparator;

public abstract class CompoundExpression<T> extends MutableExpressionImpl<T> {
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
				getWriteLock().lock();

				evaluated = false;
				postUpdate();

				getWriteLock().unlock();
			}
		}
	}

	@Override
	public final T getValue() {
		Set<Expression<?>> dependencies = getDependencies();

		synchronized (dependencies) {
			if (!evaluated) {
				getReadLock().lock();
				if (parallel)
					for (Expression<?> dependency : dependencies) {
						dependency.getReadLock().lock();
						new Thread(() -> dependency.getValue()).run();
					}

				value = evaluate();
				evaluated = true;

				if (parallel)
					for (Expression<?> dependency : dependencies)
						dependency.getReadLock().unlock();
				getReadLock().unlock();
			}
		}

		return value;
	}

	public final boolean isEvaluated() {
		return evaluated;
	}

	/**
	 * All dependency {@link Expression}s are guaranteed to be read locked for the
	 * duration of any internal invocation of this method. It may be useful to
	 * relinquish read locks before termination of this method where possible.
	 * 
	 * @return The value of this {@link Expression} as derived from the dependency
	 *         {@link Expression}s.
	 */
	protected abstract T evaluate();

	protected SortedExpressionSet<?, Expression<?>> getDependencies() {
		return dependencies;
	}
}

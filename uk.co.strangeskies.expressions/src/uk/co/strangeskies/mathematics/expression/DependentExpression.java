/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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

import static uk.co.strangeskies.utilities.EqualityComparator.identityComparator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

import uk.co.strangeskies.mathematics.expression.collection.ExpressionSetDecorator;
import uk.co.strangeskies.mathematics.expression.collection.SortedExpressionSet;

/**
 * TODO
 * 
 * @author Elias N Vasylenko
 * @param <S>
 *          the self-bound of the expression, i.e. the type of the expression
 *          object itself
 * @param <T>
 *          The type of the expression.
 */
public abstract class DependentExpression<S extends Expression<S, T>, T> extends ExpressionImpl<S, T> {
	private final SortedExpressionSet<?, Expression<?, ?>> dependencies;

	private T value;

	private final boolean parallel;

	public DependentExpression(Collection<? extends Expression<?, ?>> dependencies) {
		this(dependencies, false);
	}

	public DependentExpression(Collection<? extends Expression<?, ?>> dependencies, boolean parallel) {
		TreeSet<Expression<?, ?>> dependenciesComponent = new TreeSet<>(identityComparator());
		dependenciesComponent.addAll(dependencies);
		this.dependencies = new ExpressionSetDecorator<>(dependenciesComponent);
		this.dependencies.addObserver(m -> {
			beginWrite();
			endWrite();
		});

		this.parallel = parallel;
	}

	public DependentExpression(Expression<?, ?>... dependencies) {
		this(Arrays.asList(dependencies));
	}

	public DependentExpression(boolean parallel) {
		this(Collections.emptySet(), parallel);
	}

	@Override
	public final T getValueImpl(boolean dirty) {
		if (dirty) {
			for (Expression<?, ?> dependency : dependencies) {
				if (parallel)
					new Thread(() -> dependency.getValue()).run();
				else
					dependency.getValue();
			}

			value = evaluate();
		}

		return value;
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

	protected SortedExpressionSet<?, Expression<?, ?>> getDependencies() {
		return dependencies;
	}
}

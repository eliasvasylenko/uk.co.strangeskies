/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. l      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' l   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.expressions.
 *
 * uk.co.strangeskies.expressions is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.expressions is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.expression;

import java.util.Arrays;

/**
 * An {@link Expression} whose primary dependency is conditional on a
 * {@link Boolean} {@link Expression} dependency. The primary dependency, in
 * this instance, provides the value of this {@link Expression} directly.
 * 
 * @author Elias N Vasylenko
 * @param <O>
 *          The type of the expression.
 */
public class ConditionalExpression<O> extends DependentExpression<ConditionalExpression<O>, O> {
	private final Expression<?, ? extends /*  */Boolean> condition;
	private final Expression<?, ? extends O> expressionWhenFulfilled;
	private final Expression<?, ? extends O> expressionWhenUnfulfilled;

	/**
	 * @param condition
	 *          The condition to switch between primary dependencies.
	 * @param expressionWhenFulfilled
	 *          The {@link Expression} to set as primary dependency when the given
	 *          condition is fulfilled.
	 * @param expressionWhenUnfulfilled
	 *          The {@link Expression} to set as primary dependency when the given
	 *          condition is unfulfilled.
	 */
	public ConditionalExpression(Expression<?, ? extends /*  */Boolean> condition,
			Expression<?, ? extends O> expressionWhenFulfilled, Expression<?, ? extends O> expressionWhenUnfulfilled) {
		super(Arrays.asList(condition), false);

		if (condition == expressionWhenFulfilled || condition == expressionWhenUnfulfilled) {
			throw new IllegalArgumentException("The Condition is the same reference as one or more other Expressions.");
		}

		this.condition = condition;
		this.expressionWhenFulfilled = expressionWhenFulfilled;
		this.expressionWhenUnfulfilled = expressionWhenUnfulfilled;

		getDependencies().add(condition);
		if (condition.getValue()) {
			getDependencies().add(expressionWhenFulfilled);
		} else {
			getDependencies().add(expressionWhenUnfulfilled);
		}
	}

	@Override
	protected final O evaluate() {
		if (condition.getValue()) {
			getDependencies().remove(expressionWhenUnfulfilled);
			getDependencies().add(expressionWhenFulfilled);
			return expressionWhenFulfilled.getValue();
		} else {
			getDependencies().remove(expressionWhenFulfilled);
			getDependencies().add(expressionWhenUnfulfilled);
			return expressionWhenUnfulfilled.getValue();
		}
	}

	/**
	 * @return The condition to switch between primary dependencies.
	 */
	public final Expression<?, ? extends /*  */Boolean> getCondition() {
		return condition;
	}

	/**
	 * @return The {@link Expression} which behaves as primary dependency when the
	 *         given condition is fulfilled.
	 */
	public final Expression<?, ? extends O> getExpressionWhenFulfilled() {
		return expressionWhenFulfilled;
	}

	/**
	 * @return The {@link Expression} which behaves as primary dependency when the
	 *         given condition is unfulfilled.
	 */
	public final Expression<?, ? extends O> getExpressionWhenUnfulfilled() {
		return expressionWhenUnfulfilled;
	}

	@Override
	public ConditionalExpression<O> copy() {
		return new ConditionalExpression<>(condition, expressionWhenFulfilled, expressionWhenUnfulfilled);
	}
}

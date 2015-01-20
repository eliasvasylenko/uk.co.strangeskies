/**
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.
 *
 *     uk.co.strangeskies is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     uk.co.strangeskies is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with uk.co.strangeskies.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.expression;

import java.util.Arrays;

public class ConditionalForwardingExpression<O> extends CompoundExpression<O> {
	private final Expression<? extends /*  */Boolean> condition;
	private final Expression<? extends O> expressionWhenFulfilled;
	private final Expression<? extends O> expressionWhenUnfulfilled;

	public ConditionalForwardingExpression(
			Expression<? extends /*  */Boolean> condition,
			Expression<? extends O> expressionWhenFulfilled,
			Expression<? extends O> expressionWhenUnfulfilled) {
		super(Arrays.asList(condition), false);

		if (condition == expressionWhenFulfilled
				|| condition == expressionWhenUnfulfilled) {
			throw new IllegalArgumentException(
					"The Condition is the same reference as one or more other Expressions.");
		}

		this.condition = condition;
		this.expressionWhenFulfilled = expressionWhenFulfilled;
		this.expressionWhenUnfulfilled = expressionWhenUnfulfilled;

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

	public final Expression<? extends /*  */Boolean> getCondition() {
		return condition;
	}

	public final Expression<? extends O> getExpressionWhenFulfilled() {
		return expressionWhenFulfilled;
	}

	public final Expression<? extends O> getExpressionWhenUnfulfilled() {
		return expressionWhenUnfulfilled;
	}
}

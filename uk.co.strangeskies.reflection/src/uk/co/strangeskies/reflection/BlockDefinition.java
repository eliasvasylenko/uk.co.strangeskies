/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

import java.util.ArrayList;
import java.util.List;

public abstract class BlockDefinition<S extends BlockDefinition<S>> {
	private final StaticScope scope;
	private final List<Statement> statements;

	public BlockDefinition() {
		this(StaticScope.create());
	}

	public BlockDefinition(StaticScope scope) {
		this.scope = scope;
		statements = new ArrayList<>();
	}

	public S addExpression(Expression expression) {
		return addStatement(expression.asStatement());
	}

	@SuppressWarnings("unchecked")
	protected S addStatement(Statement statement) {
		statements.add(statement);

		return (S) this;
	}

	public <T> VariableExpression<T> declareVariable(Class<T> type) {
		return declareVariable(TypeToken.over(type));
	}

	public <T> VariableExpression<T> declareVariable(TypeToken<T> type) {
		return scope.declareVariable(type);
	}

	public Object execute(State state) {
		for (Statement statement : statements) {
			statement.execute(state);

			if (state.isReturned()) {
				return state.getReturnValue();
			}
		}

		return null;
	}
}

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

import uk.co.strangeskies.utilities.Self;

public abstract class BlockDefinition<S extends BlockDefinition<S>> implements Self<S> {
	private final List<Statement> statements;

	public BlockDefinition() {
		statements = new ArrayList<>();
	}

	public BlockDefinition(BlockDefinition<S> copy) {
		this();

		for (Statement statement : copy.statements)
			addStatement(statement);
	}

	public S addExpression(Expression expression) {
		return addStatement(expression.asStatement());
	}

	protected S addStatement(Statement statement) {
		statements.add(statement);

		return getThis();
	}

	public <T> VariableExpression<T> declareVariable(Class<T> type) {
		return declareVariable(type, ValueExpression.nullExpression());
	}

	public <T> VariableExpression<T> declareVariable(TypeToken<T> type) {
		return declareVariable(type, ValueExpression.nullExpression());
	}

	public <T> VariableExpression<T> declareVariable(Class<T> type, ValueExpression<? extends T> value) {
		return declareVariable(TypeToken.over(type), value);
	}

	public <T> VariableExpression<T> declareVariable(TypeToken<T> type, ValueExpression<? extends T> value) {
		LocalVariableExpression<T> variable = new LocalVariableExpression<>(type);

		addStatement(new VariableDeclaration<>(variable, value));

		return variable;
	}

	public <T> ValueExpression<T> declareValue(Class<T> type, ValueExpression<? extends T> value) {
		return declareValue(TypeToken.over(type), value);
	}

	public <T> ValueExpression<T> declareValue(TypeToken<T> type, ValueExpression<? extends T> value) {
		LocalValueExpression<T> variable = new LocalValueExpression<>(type);

		addStatement(new VariableDeclaration<>(variable, value));

		return variable;
	}

	public Object execute(State state) {
		state = state.enclose();

		for (Statement statement : statements) {
			statement.execute(state);

			if (state.isReturned()) {
				return state.getReturnValue();
			}
		}

		return null;
	}
}

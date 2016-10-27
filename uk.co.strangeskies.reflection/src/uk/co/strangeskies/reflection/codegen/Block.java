/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
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
package uk.co.strangeskies.reflection.codegen;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.utilities.Self;

public class Block<T> implements Self<Block<T>> {
	private final List<Statement> statements;

	public Block() {
		statements = new ArrayList<>();
	}

	public Block(Block<T> copy) {
		this();

		for (Statement statement : copy.statements)
			addStatement(statement);
	}

	@Override
	public Block<T> copy() {
		return new Block<>(this);
	}

	public Stream<Statement> getStatements() {
		return statements.stream();
	}

	protected Block<T> addStatement(Statement statement) {
		statements.add(statement);

		return getThis();
	}

	public Block<T> addExpression(Expression expression) {
		return addStatement(v -> v.visitExpression(expression));
	}

	public <U> LocalVariableExpression<U> declareVariable(Class<U> type) {
		return declareVariable(TypeToken.overType(type));
	}

	public <U> LocalVariableExpression<U> declareVariable(TypeToken<U> type) {
		LocalVariableExpression<U> variable = new LocalVariableExpression<>(type);

		addStatement(s -> s.visitDeclaration(variable));

		return variable;
	}

	public <U> LocalVariableExpression<U> declareVariable(Class<U> type, ValueExpression<? extends U> value) {
		return declareVariable(TypeToken.overType(type), value);
	}

	public <U> LocalVariableExpression<U> declareVariable(TypeToken<U> type, ValueExpression<? extends U> value) {
		LocalVariableExpression<U> variable = new LocalVariableExpression<>(type);

		addStatement(s -> s.visitDeclaration(variable, value));

		return variable;
	}

	public <U> LocalValueExpression<U> declareValue(Class<U> type, ValueExpression<? extends U> value) {
		return declareValue(TypeToken.overType(type), value);
	}

	public <U> LocalValueExpression<U> declareValue(TypeToken<U> type, ValueExpression<? extends U> value) {
		LocalValueExpression<U> variable = new LocalValueExpression<>(type);

		addStatement(s -> s.visitDeclaration(variable, value));

		return variable;
	}

	public Block<T> addReturnStatement() {
		addStatement(StatementVisitor::visitReturn);

		return this;
	}

	public Block<T> addReturnStatement(ValueExpression<? extends T> expression) {
		addStatement(v -> v.visitReturn(expression));

		return this;
	}
}

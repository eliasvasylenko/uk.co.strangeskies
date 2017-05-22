/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
 * This file is part of uk.co.strangeskies.reflection.codegen.
 *
 * uk.co.strangeskies.reflection.codegen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection.codegen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.codegen.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.token.TypeToken;

public class Block<T> {
	private final List<Statement> statements;

	public Block() {
		this.statements = Collections.emptyList();
	}

	protected Block(List<Statement> statements) {
		this.statements = statements;
	}

	public Stream<Statement> getStatements() {
		return statements.stream();
	}

	protected Block<T> withStatement(Statement statement) {
		List<Statement> statements = new ArrayList<>(this.statements.size() + 1);
		statements.addAll(this.statements);
		statements.add(statement);

		return new Block<>(statements);
	}

	public Block<T> withExpression(Expression expression) {
		return withStatement(v -> v.visitExpression(expression));
	}

	public <U> LocalVariableExpression<U> withVariableDeclaration(String name, Class<U> type) {
		return declareVariable(name, TypeToken.forClass(type));
	}

	public <U> LocalVariableExpression<U> declareVariable(String name, TypeToken<U> type) {
		LocalVariableExpression<U> variable = new LocalVariableExpression<>(name, type);

		withStatement(s -> s.visitDeclaration(variable));

		return variable;
	}

	public <U> LocalVariableExpression<U> declareVariable(
			String name,
			Class<U> type,
			ValueExpression<? extends U> value) {
		return declareVariable(name, TypeToken.forClass(type), value);
	}

	public <U> LocalVariableExpression<U> declareVariable(
			String name,
			TypeToken<U> type,
			ValueExpression<? extends U> value) {
		LocalVariableExpression<U> variable = new LocalVariableExpression<>(name, type);

		withStatement(s -> s.visitDeclaration(variable, value));

		return variable;
	}

	public <U> LocalValueExpression<U> declareValue(String name, Class<U> type, ValueExpression<? extends U> value) {
		return declareValue(name, TypeToken.forClass(type), value);
	}

	public <U> LocalValueExpression<U> declareValue(String name, TypeToken<U> type, ValueExpression<? extends U> value) {
		LocalValueExpression<U> variable = new LocalValueExpression<>(name, type);

		withStatement(s -> s.visitDeclaration(variable, value));

		return variable;
	}

	public Block<T> withReturnStatement() {
		return withStatement(StatementVisitor::visitReturn);
	}

	public Block<T> withReturnStatement(ValueExpression<? extends T> expression) {
		return withStatement(v -> v.visitReturn(expression));
	}
}

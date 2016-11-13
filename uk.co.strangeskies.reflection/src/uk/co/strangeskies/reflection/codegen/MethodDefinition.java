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

import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.TypeToken;

public abstract class MethodDefinition<C, T> implements MemberDefinition<C> {
	private final MethodDeclaration<C, T> classDeclaration;
	private final Block<T> body;

	@SuppressWarnings("unchecked")
	public MethodDefinition(MethodDeclaration<C, T> classDeclaration) {
		this.classDeclaration = classDeclaration;
		this.body = new Block<>();
	}

	@SuppressWarnings("unchecked")
	public List<VariableExpression<?>> getParameters() {
		return (List<VariableExpression<?>>) (List<?>) parameters;
	}

	public TypeToken<T> getReturnType() {
		return returnType;
	}

	public ErasedMethodSignature getOverrideSignature() {
		return overrideSignature;
	}

	public Block<T> body() {
		return body;
	}

	public MethodDeclaration<C, T> getDeclaration() {

	}

	@Override
	public ClassDeclaration<?, C> getClassDeclaration() {
		return classDeclaration;
	}

	@Override
	public String getName() {
		return getd;
	}

	@Override
	public int getModifiers() {
		// TODO
		return 0;
	}

	@Override
	public boolean isSynthetic() {
		return false;
	}

	public void validate() {
		/*
		 * TODO check all expressions in body are in scope
		 */
	}

	protected T invoke(StatementExecutor state, Object[] arguments) {
		int i = 0;
		for (LocalVariableExpression<?> parameter : parameters) {
			setParameterUnsafe(state, parameter.getId(), arguments[i]);
		}

		return state.executeBlock(body);
	}

	@SuppressWarnings("unchecked")
	private static <T> void setParameterUnsafe(StatementExecutor state, LocalVariable<T> parameter, Object argument) {
		state.declareLocal(parameter);
		state.setEnclosedLocal(parameter, (T) argument);
	}

	public ExecutableToken<C, T> asToken() {
		// TODO Auto-generated method stub
		return null;
	}

	public MethodDefinition<C, T> withBody(Function<Block<T>, Block<T>> object) {
		// TODO Auto-generated method stub
		return null;
	}

	public <U> LocalVariableExpression<U> getParameter(VariableSignature<U> parameterSignature) {
		return null;
	}
}

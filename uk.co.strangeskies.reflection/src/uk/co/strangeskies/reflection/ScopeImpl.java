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

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a static, compile-time scope for evaluation of {@link Expression
 * java expressions}. A scope defines which local variables are available, as
 * well as the enclosing type of the expression (i.e. the type of the "this"
 * reference).
 * <p>
 * Available local variables may conceptually include method parameters of any
 * enclosing methods or lambdas.
 * 
 * @author Elias N Vasylenko
 */
public abstract class ScopeImpl implements Scope {
	public class LocalVariableExpression<T> implements VariableExpression<T> {
		private final Scope scope;
		private final TypeToken<T> type;

		public LocalVariableExpression(Scope scope, TypeToken<T> type) {
			this.scope = scope;
			this.type = type;
		}

		@Override
		public TypeToken<T> getType() {
			return type;
		}

		@Override
		public VariableResult<T> evaluate(State state) {
			return new VariableResult<T>() {
				@Override
				public T get() {
					return state.getEnclosingScopeLocals(scope).get(LocalVariableExpression.this);
				}

				@Override
				public void set(T value) {
					state.getEnclosingScopeLocals(scope).set(LocalVariableExpression.this, value);
				}
			};
		}
	}

	private final Scope enclosingScope;
	private final Set<VariableExpression<?>> variableExpressions;

	public ScopeImpl() {
		this(null);
	}

	@Override
	public Scope getEnclosingScope() {
		return enclosingScope;
	}

	public ScopeImpl(Scope enclosingScope) {
		this.enclosingScope = enclosingScope;
		variableExpressions = new HashSet<>();
	}

	protected <T> VariableExpression<T> defineVariable(TypeToken<T> type) {
		VariableExpression<T> variable = new LocalVariableExpression<>(this, type);

		variableExpressions.add(variable);

		return variable;
	}
}

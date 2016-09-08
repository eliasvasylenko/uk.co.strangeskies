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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
 *
 * @param <I>
 *          the type of the enclosing instance
 */
public class ScopeImpl<I> implements Scope<I> {
	private final TypeToken<I> receiverType;
	private final ValueExpression<I> receiverExpression;
	private final Set<VariableExpression<?>> variableExpressions;

	public ScopeImpl(TypeToken<I> receiverType) {
		this.receiverType = receiverType;
		this.receiverExpression = new ValueExpression<I>() {
			@Override
			public ValueResult<I> evaluate(State state) {
				return () -> state.getEnclosingInstance(ScopeImpl.this);
			}

			@Override
			public TypeToken<I> getType() {
				return getReceiverType();
			}
		};
		variableExpressions = new HashSet<>();
	}

	@Override
	public TypeToken<I> getReceiverType() {
		return receiverType;
	}

	@Override
	public ValueExpression<I> receiver() {
		return receiverExpression;
	}

	@Override
	public <T> VariableExpression<T> defineLocal(TypeToken<T> type) {
		VariableExpression<T> variable = new LocalVariableExpression<>(this, type);

		variableExpressions.add(variable);

		return variable;
	}

	@Override
	public State initializeState(I instance) {
		Locals locals = new Locals() {
			private final Map<VariableExpression<?>, Object> localValues = new HashMap<>();

			@Override
			public <T> void set(VariableExpression<T> variableResult, T value) {
				localValues.put(variableResult, value);
			}

			@SuppressWarnings("unchecked")
			@Override
			public <T> T get(VariableExpression<T> variableResult) {
				return (T) localValues.get(variableResult);
			}
		};

		return new State() {
			@Override
			public ScopeImpl<?> getScope() {
				return ScopeImpl.this;
			}

			@Override
			public Locals getEnclosingScopeLocals(Scope<?> scope) {
				return locals;
			}

			@SuppressWarnings("unchecked")
			@Override
			public <J> J getEnclosingInstance(Scope<J> parentScope) {
				if (parentScope == ScopeImpl.this) {
					return (J) instance;
				} else {
					throw new ReflectionException(p -> p.noEnclosingInstance(parentScope));
				}
			}
		};
	}
}

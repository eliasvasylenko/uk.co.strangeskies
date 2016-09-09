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
import java.util.Map;

public class StateImpl implements State {
	private final State enclosingState;
	private final Scope scope;
	private final Locals locals;

	public StateImpl(Scope scope) {
		this(null, scope);
	}

	public StateImpl(State enclosingState, Scope scope) {
		this.enclosingState = enclosingState;
		this.scope = scope;

		locals = new Locals() {
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
	}

	@Override
	public Scope getScope() {
		return scope;
	}

	@Override
	public Locals getEnclosingScopeLocals(Scope scope) {
		if (scope == this.scope) {
			return locals;
		} else if (enclosingState != null) {
			return enclosingState.getEnclosingScopeLocals(scope);
		} else {
			throw new ReflectionException(p -> p.cannotResolveEnclosingScope(scope));
		}
	}

	@Override
	public <J> J getEnclosingInstance(InstanceScope<J> parentScope) {
		if (enclosingState != null) {
			return enclosingState.getEnclosingInstance(parentScope);
		} else {
			throw new ReflectionException(p -> p.cannotResolveEnclosingScope(parentScope));
		}
	}
}

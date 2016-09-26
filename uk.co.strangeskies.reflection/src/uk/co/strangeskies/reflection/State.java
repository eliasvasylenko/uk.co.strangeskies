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

public class State implements StatementVisitor {
	private final ReflectiveInstance<?> receiver;
	private final State enclosingState;

	private final Map<LocalValueExpression<?>, Object> locals;

	private boolean returned;
	private Object returnValue;

	private State(ReflectiveInstance<?> receiver, State enclosingState) {
		this.receiver = receiver;
		this.enclosingState = enclosingState;

		locals = new HashMap<>();
	}

	public State(ReflectiveInstance<?> receiver) {
		this(receiver, null);
	}

	public State(State enclosingState) {
		this(null, enclosingState);
	}

	public State() {
		this(null, null);
	}

	@SuppressWarnings("unchecked")
	public <T> T getEnclosedLocal(LocalValueExpression<T> variable) {
		if (locals.containsKey(variable)) {
			return (T) locals.get(variable);
		} else if (enclosingState != null) {
			return enclosingState.getEnclosedLocal(variable);
		} else {
			throw new ReflectionException(p -> p.undefinedVariable(variable));
		}
	}

	public <T> void setEnclosedLocal(LocalVariableExpression<T> variable, T value) {
		if (locals.containsKey(variable)) {
			locals.put(variable, value);
		} else if (enclosingState != null) {
			enclosingState.setEnclosedLocal(variable, value);
		} else {
			throw new ReflectionException(p -> p.undefinedVariable(variable));
		}
	}

	@SuppressWarnings("unchecked")
	public <J> J getEnclosingInstance(ClassDefinition<J> receiverClass) {
		if (receiver.getReflectiveClassDefinition() == receiverClass) {
			return (J) receiver;
		} else if (enclosingState != null) {
			return enclosingState.getEnclosingInstance(receiverClass);
		} else {
			throw new ReflectionException(p -> p.cannotResolveEnclosingInstance(receiverClass));
		}
	}

	public <T> void visitBlock(Block block) {
		state = state.enclose();

		block.getStatements().forEach(statement -> {
			statement.execute(state);

			if (state.isReturned()) {
				return state.getReturnValue();
			}
		});

		return null;
	}

	@Override
	public void visitReturn() {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> void visitReturn(ValueExpression<T> expression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitExpression(Expression expression) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> void visitDeclaration(LocalVariableExpression<T> variable) {
		if (locals.containsKey(variable)) {
			throw new ReflectionException(p -> p.cannotRedefineVariable(variable));
		}
		locals.put(variable, null);
	}

	@Override
	public <T> void visitDeclaration(LocalValueExpression<T> value, ValueExpression<? extends T> initializer) {
		if (locals.containsKey(value)) {
			throw new ReflectionException(p -> p.cannotRedefineVariable(value));
		}
		locals.put(value, null); // TODO get value from initializer
	}
}

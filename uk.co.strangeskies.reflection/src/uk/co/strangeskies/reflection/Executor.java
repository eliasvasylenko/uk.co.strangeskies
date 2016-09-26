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

public class Executor implements StatementVisitor {
	private final ReflectiveInstance<?> receiver;
	private final Executor enclosingState;

	private final Map<LocalVariable<?>, Object> locals;

	private boolean returned;
	private Object returnValue;

	private Executor(ReflectiveInstance<?> receiver, Executor enclosingState) {
		this.receiver = receiver;
		this.enclosingState = enclosingState;

		locals = new HashMap<>();
	}

	public Executor(ReflectiveInstance<?> receiver) {
		this(receiver, null);
	}

	public Executor() {
		this(null);
	}

	public Executor enclose(ReflectiveInstance<?> receiver) {
		return new Executor(receiver, this);
	}

	public Executor enclose() {
		return enclose(null);
	}

	public <T> void declareLocal(LocalVariable<T> variable) {
		if (locals.containsKey(variable)) {
			throw new ReflectionException(p -> p.cannotRedeclareVariable(variable));
		}
		locals.put(variable, null);
	}

	@SuppressWarnings("unchecked")
	public <T> T getEnclosedLocal(LocalVariable<T> variable) {
		if (locals.containsKey(variable)) {
			return (T) locals.get(variable);
		} else if (enclosingState != null) {
			return enclosingState.getEnclosedLocal(variable);
		} else {
			throw new ReflectionException(p -> p.undefinedVariable(variable));
		}
	}

	public <T> void setEnclosedLocal(LocalVariable<T> variable, T value) {
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

	@SuppressWarnings("unchecked")
	public <T> T executeBlock(TypedBlock<T> block) {
		return (T) executeBlockImpl(block);
	}

	public void executeBlock(VoidBlock block) {
		executeBlockImpl(block);
	}

	private Object executeBlockImpl(Block<?> block) {
		Executor state = enclose();

		return block.getStatements().map(s -> {
			s.accept(state);
			return state.getReturnedValue();
		}).filter(s -> state.isReturned()).findAny().orElse(null);
	}

	private boolean isReturned() {
		// TODO Auto-generated method stub
		return null;
	}

	private Object getReturnedValue() {
		// TODO Auto-generated method stub
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
		declareLocal(variable.getId());
	}

	@Override
	public <T> void visitDeclaration(LocalValueExpression<T> variable, ValueExpression<? extends T> initializer) {
		declareLocal(variable.getId());
		setEnclosedLocal(variable.getId(), null); // TODO get value from initializer
	}
}

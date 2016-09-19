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

import java.lang.reflect.AnnotatedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MethodDefinition<C, T> extends ParameterizedDefinition<MethodDefinition<C, T>>
		implements MemberDefinition<C, T> {
	private final ClassDefinition<C> classDefinition;
	private final String methodName;
	private final StaticScope scope;

	private final List<VariableExpression<?>> parameters;
	private final TypeToken<T> returnType;
	private final TypedBlockDefinition<T> body;

	private final MethodSignature overrideSignature;

	@SuppressWarnings("unchecked")
	public MethodDefinition(MethodDeclaration<C, T> declaration) {
		super(declaration);

		this.classDefinition = declaration.getClassDefinition();
		this.methodName = declaration.getName();
		this.scope = new StaticScopeImpl(classDefinition);

		ArrayList<VariableExpression<?>> parameters = new ArrayList<>();
		for (VariableExpressionProxy<?> proxy : declaration.getParameters()) {
			parameters.add(getParameter(proxy, declaration.getParameterType(proxy)));
		}
		parameters.trimToSize();
		this.parameters = Collections.unmodifiableList(parameters);
		if (declaration.getReturnType() == null) {
			this.returnType = (TypeToken<T>) TypeToken.over(void.class);
		} else {
			this.returnType = (TypeToken<T>) TypeToken.over(declaration.getReturnType());
		}
		this.body = new TypedBlockDefinition<>(scope);

		overrideSignature = new MethodSignature(methodName,
				parameters.stream().map(v -> v.getType().getRawType()).toArray(Class<?>[]::new));

		classDefinition.overrideMethod(this);
	}

	public List<VariableExpression<?>> getParameters() {
		return parameters;
	}

	public TypeToken<T> getReturnType() {
		return returnType;
	}

	public MethodSignature getOverrideSignature() {
		return overrideSignature;
	}

	private <U> VariableExpression<U> getParameter(VariableExpressionProxy<U> proxy, AnnotatedType type) {
		TypeToken<?> typeToken = TypeToken.over(substituteTypeVariableSignatures(type));

		@SuppressWarnings("unchecked")
		VariableExpression<U> variable = (VariableExpression<U>) scope.declareVariable(typeToken);
		proxy.setComponent(variable);

		return variable;
	}

	public TypedBlockDefinition<T> body() {
		return body;
	}

	@Override
	public Class<?> getDeclaringClass() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ClassDefinition<C> getDeclaringClassDefinition() {
		return classDefinition;
	}

	@Override
	public String getName() {
		return methodName;
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

	@SuppressWarnings("unchecked")
	public T invoke(State state, Object[] arguments) {
		state = state.enclose(scope);

		int i = 0;
		for (VariableExpression<?> parameter : parameters) {
			setParameterUnsafe(state, parameter, arguments[i]);
		}

		return (T) body().execute(state);
	}

	@SuppressWarnings("unchecked")
	private <T> void setParameterUnsafe(State state, VariableExpression<T> parameter, Object argument) {
		parameter.evaluate(state).set((T) argument);
	}
}

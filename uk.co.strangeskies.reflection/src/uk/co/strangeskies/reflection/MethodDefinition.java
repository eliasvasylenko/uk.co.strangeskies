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
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MethodDefinition<C, T> extends GenericDefinition<MethodDefinition<C, T>> {
	public static class MethodSignature<C, T> extends GenericSignature {
		private final ClassDefinition<C> classDefinition;
		private final String methodName;

		private final LinkedHashMap<VariableExpressionProxy<?>, AnnotatedType> parameters;
		private AnnotatedType returnType;

		protected MethodSignature(ClassDefinition<C> classDefinition, String methodName) {
			this.classDefinition = classDefinition;
			this.methodName = methodName;

			parameters = new LinkedHashMap<>();
		}

		protected String getMethodName() {
			return methodName;
		}

		protected AnnotatedType getReturnType() {
			return returnType;
		}

		public MethodDefinition<C, T> define() {
			return new MethodDefinition<>(this);
		}

		public VariableExpression<?> addParameter(AnnotatedType type) {
			VariableExpressionProxy<?> proxy = new VariableExpressionProxy<>();
			parameters.put(proxy, type);
			return proxy;
		}

		public VariableExpression<?> addParameter(Type type) {
			return addParameter(AnnotatedTypes.over(type));
		}

		@SuppressWarnings("unchecked")
		public <U> VariableExpression<U> addParameter(Class<U> type) {
			return (VariableExpression<U>) addParameter(AnnotatedTypes.over(type));
		}

		@SuppressWarnings("unchecked")
		public <U> VariableExpression<U> addParameter(TypeToken<U> type) {
			return (VariableExpression<U>) addParameter(type.getAnnotatedDeclaration());
		}

		public MethodSignature<C, T> withParameter(AnnotatedType type) {
			parameters.put(new VariableExpressionProxy<>(), type);
			return this;
		}

		public MethodSignature<C, T> withParameter(Type type) {
			return withParameter(AnnotatedTypes.over(type));
		}

		public MethodSignature<C, T> withParameter(TypeToken<?> type) {
			return withParameter(type.getAnnotatedDeclaration());
		}

		public MethodSignature<C, T> withReturnType(AnnotatedType type) {
			returnType = type;
			Method d;
			return this;
		}

		public MethodSignature<C, T> withReturnType(Type type) {
			return withReturnType(AnnotatedTypes.over(type));
		}

		@SuppressWarnings("unchecked")
		public <U extends T> MethodSignature<C, U> withReturnType(Class<U> type) {
			return (MethodSignature<C, U>) withReturnType(AnnotatedTypes.over(type));
		}

		@SuppressWarnings("unchecked")
		public <U extends T> MethodSignature<C, U> withReturnType(TypeToken<U> type) {
			return (MethodSignature<C, U>) withReturnType(type.getAnnotatedDeclaration());
		}
	}

	private final ClassDefinition<C> classDefinition;
	private final String methodName;
	private final StaticScope scope;

	private final List<VariableExpression<?>> parameters;
	private final AnnotatedType returnType;
	private final TypedBlockDefinition<T> body;

	public MethodDefinition(MethodSignature<C, T> signature) {
		super(signature);

		this.classDefinition = signature.classDefinition;
		this.methodName = signature.methodName;
		this.scope = new StaticScopeImpl();

		ArrayList<VariableExpression<?>> parameters = new ArrayList<>();
		for (Map.Entry<VariableExpressionProxy<?>, AnnotatedType> proxy : signature.parameters.entrySet()) {
			parameters.add(getParameter(proxy.getKey(), proxy.getValue()));
		}
		parameters.trimToSize();
		this.parameters = Collections.unmodifiableList(parameters);
		this.returnType = signature.returnType;
		this.body = new TypedBlockDefinition<>(scope);

		classDefinition.addMethod(this);
	}

	private <U> VariableExpression<U> getParameter(VariableExpressionProxy<U> proxy, AnnotatedType type) {
		TypeToken<?> typeToken = TypeToken.over(substituteTypeVariableSignatures(type));

		@SuppressWarnings("unchecked")
		VariableExpression<U> variable = (VariableExpression<U>) scope.defineVariable(typeToken);
		proxy.setComponent(variable);

		return variable;
	}

	public TypedBlockDefinition<T> body() {
		return body;
	}
}

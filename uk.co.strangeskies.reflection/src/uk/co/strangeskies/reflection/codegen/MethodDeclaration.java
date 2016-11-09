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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.token.TypeToken;

public class MethodDeclaration<C, T> extends ParameterizedDeclaration<MethodDeclaration<C, T>>
		implements MemberDeclaration<C> {
	protected static <C> MethodDeclaration<C, Void> declareMethod(String methodName) {
		return new MethodDeclaration<>(methodName);
	}

	private final String methodName;

	private final Map<VariableExpressionProxy<?>, AnnotatedType> parameters;
	private final AnnotatedType returnType;

	protected MethodDeclaration(String methodName) {
		this.methodName = methodName;
		this.parameters = new LinkedHashMap<>();
		this.returnType = AnnotatedTypes.over(void.class);
	}

	protected MethodDeclaration(
			String methodName,
			Map<VariableExpressionProxy<?>, AnnotatedType> parameters,
			AnnotatedType returnType,
			Collection<? extends TypeVariableDeclaration> typeVariables,
			Collection<? extends Annotation> annotations) {
		super(typeVariables, annotations);

		this.methodName = methodName;
		this.parameters = parameters;
		this.returnType = returnType;
	}

	@Override
	protected MethodDeclaration<C, T> withParameterizedDeclarationData(
			Collection<? extends TypeVariableDeclaration> typeVariables,
			Collection<? extends Annotation> annotations) {
		return new MethodDeclaration<>(methodName, parameters, returnType, typeVariables, annotations);
	}

	public Stream<VariableExpressionProxy<?>> getParameters() {
		return parameters.keySet().stream();
	}

	public AnnotatedType getParameterType(VariableExpressionProxy<?> parameter) {
		return parameters.get(parameter);
	}

	@Override
	public String getName() {
		return methodName;
	}

	public AnnotatedType getReturnType() {
		return returnType;
	}

	@Override
	public MethodDefinition<C, T> define() {
		return new InstanceMethodDefinition<>(this);
	}

	protected MethodDefinition<C, T> defineInstance() {
		return new InstanceMethodDefinition<>(this);
	}

	public MethodDefinition<C, T> withParameter(String parameterName, AnnotatedType type) {

	}

	public MethodDefinition<C, T> withParameter(String parameterName, Type type) {
		return withParameter(parameterName, AnnotatedTypes.over(type));
	}

	@SuppressWarnings("unchecked")
	public <U> MethodDefinition<C, T> withParameter(String parameterName, Class<U> type) {
		return withParameter(parameterName, AnnotatedTypes.over(type));
	}

	@SuppressWarnings("unchecked")
	public <U> MethodDefinition<C, T> withParameter(String parameterName, TypeToken<U> type) {
		return withParameter(parameterName, type.getAnnotatedDeclaration());
	}

	public MethodDeclaration<C, ?> withReturnType(AnnotatedType type) {
		return new MethodDeclaration<>(methodName, parameters, type, typeVariables, annotations);
	}

	public MethodDeclaration<C, ?> withReturnType(Type type) {
		return withReturnType(AnnotatedTypes.over(type));
	}

	@SuppressWarnings("unchecked")
	public <U> MethodDeclaration<C, U> withReturnType(Class<U> type) {
		return (MethodDeclaration<C, U>) withReturnType(AnnotatedTypes.over(type));
	}

	@SuppressWarnings("unchecked")
	public <U> MethodDeclaration<C, U> withReturnType(TypeToken<U> type) {
		return (MethodDeclaration<C, U>) withReturnType(type.getAnnotatedDeclaration());
	}
}

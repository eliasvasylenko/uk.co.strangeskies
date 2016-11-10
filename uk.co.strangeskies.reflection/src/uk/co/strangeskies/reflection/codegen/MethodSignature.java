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

import static java.util.Arrays.asList;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.token.TypeToken;

public class MethodSignature<T> extends ParameterizedSignature<MethodSignature<T>>
		implements MemberSignature<MethodSignature<T>> {
	private final String methodName;

	private final Collection<? extends VariableSignature<?>> parameters;
	private final AnnotatedType returnType;

	public MethodSignature(String methodName) {
		this.methodName = methodName;
		this.parameters = Collections.emptySet();
		this.returnType = AnnotatedTypes.over(void.class);
	}

	protected MethodSignature(
			String methodName,
			Collection<? extends VariableSignature<?>> parameters,
			AnnotatedType returnType,
			Collection<? extends TypeVariableSignature> typeVariables,
			Collection<? extends Annotation> annotations) {
		super(typeVariables, annotations);

		this.methodName = methodName;
		this.parameters = parameters;
		this.returnType = returnType;
	}

	@Override
	protected MethodSignature<T> withParameterizedDeclarationData(
			Collection<? extends TypeVariableSignature> typeVariables,
			Collection<? extends Annotation> annotations) {
		return new MethodSignature<>(methodName, parameters, returnType, typeVariables, annotations);
	}

	public Stream<? extends VariableSignature<?>> getParameters() {
		return parameters.stream();
	}

	@Override
	public String getName() {
		return methodName;
	}

	public AnnotatedType getReturnType() {
		return returnType;
	}

	protected <C> MethodDefinition<C, T> defineInstance(ClassSignature<C> classDeclaration) {
		return new InstanceMethodDefinition<>(classDeclaration, this);
	}

	public MethodSignature<T> withParameters(VariableSignature<?>... parameters) {
		return withParameters(asList(parameters));
	}

	public MethodSignature<T> withParameters(Collection<? extends VariableSignature<?>> parameters) {
		return new MethodSignature<>(methodName, new ArrayList<>(parameters), returnType, typeVariables, annotations);
	}

	public MethodSignature<?> withReturnType(AnnotatedType type) {
		return new MethodSignature<>(methodName, parameters, type, typeVariables, annotations);
	}

	public MethodSignature<?> withReturnType(Type type) {
		return withReturnType(AnnotatedTypes.over(type));
	}

	@SuppressWarnings("unchecked")
	public <U> MethodSignature<U> withReturnType(Class<U> type) {
		return (MethodSignature<U>) withReturnType(AnnotatedTypes.over(type));
	}

	@SuppressWarnings("unchecked")
	public <U> MethodSignature<U> withReturnType(TypeToken<U> type) {
		return (MethodSignature<U>) withReturnType(type.getAnnotatedDeclaration());
	}
}

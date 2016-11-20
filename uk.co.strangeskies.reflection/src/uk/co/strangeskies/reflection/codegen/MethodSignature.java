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

import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Collection;

import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.TypeToken;

public class MethodSignature<T> extends ExecutableSignature<MethodSignature<T>> {
	public static MethodSignature<Void> methodSignature(String methodName) {
		return new MethodSignature<>(methodName);
	}

	public static <U> MethodSignature<U> methodSignature(ExecutableToken<?, U> executableToken) {
		return new MethodSignature<>(executableToken.getName())
				.withTypeVariables(
						executableToken.getTypeParameters().map(TypeVariableSignature::typeVariableSignature).collect(toList()))
				.withReturnType(executableToken.getReturnType())
				.withParameters(
						executableToken.getParameters().stream().map(VariableSignature::variableSignature).collect(toList()));
	}

	private final String methodName;

	private final AnnotatedType returnType;

	protected MethodSignature(String methodName) {
		this.methodName = methodName;
		this.returnType = AnnotatedTypes.annotated(void.class);
	}

	protected MethodSignature(
			String methodName,
			Collection<? extends VariableSignature<?>> parameters,
			AnnotatedType returnType,
			Collection<? extends TypeVariableSignature> typeVariables,
			Collection<? extends Annotation> annotations) {
		super(parameters, typeVariables, annotations);

		this.methodName = methodName;
		this.returnType = returnType;
	}

	@Override
	protected MethodSignature<T> withExecutableSignatureData(
			Collection<? extends VariableSignature<?>> parameters,
			Collection<? extends TypeVariableSignature> typeVariables,
			Collection<? extends Annotation> annotations) {
		return new MethodSignature<>(methodName, parameters, returnType, typeVariables, annotations);
	}

	public String getName() {
		return methodName;
	}

	public AnnotatedType getReturnType() {
		return returnType;
	}

	public MethodSignature<?> withReturnType(AnnotatedType type) {
		return new MethodSignature<>(methodName, parameters, type, typeVariables, annotations);
	}

	public MethodSignature<?> withReturnType(Type type) {
		return withReturnType(AnnotatedTypes.annotated(type));
	}

	@SuppressWarnings("unchecked")
	public <U> MethodSignature<U> withReturnType(Class<U> type) {
		return (MethodSignature<U>) withReturnType(AnnotatedTypes.annotated(type));
	}

	@SuppressWarnings("unchecked")
	public <U> MethodSignature<U> withReturnType(TypeToken<U> type) {
		return (MethodSignature<U>) withReturnType(type.getAnnotatedDeclaration());
	}

	@Override
	public String toString() {
		return super.toString();
	}
}

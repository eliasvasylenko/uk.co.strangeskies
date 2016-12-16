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
import java.util.List;
import java.util.Objects;
import java.util.Set;

import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.Visibility;
import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.TypeToken;

public class MethodSignature<T> extends ExecutableSignature<MethodSignature<T>> {
	public static MethodSignature<Void> methodSignature(String methodName) {
		return new MethodSignature<>(methodName);
	}

	public static <U> MethodSignature<U> methodSignature(ExecutableToken<?, U> executableToken) {
		return new MethodSignature<>(executableToken.getName())
				.withAnnotations(executableToken.getMember().getAnnotations())
				.withTypeVariables(
						executableToken.getTypeParameters().map(TypeVariableSignature::typeVariableSignature).collect(toList()))
				.withReturnType(executableToken.getReturnType())
				.withParameters(executableToken.getParameters().map(VariableSignature::variableSignature).collect(toList()));
	}

	private final AnnotatedType returnType;

	protected MethodSignature(String methodName) {
		super(methodName);

		this.returnType = AnnotatedTypes.annotated(void.class);
	}

	protected MethodSignature(
			String name,
			Set<Annotation> annotations,
			Visibility visibility,
			List<VariableSignature<?>> parameters,
			List<TypeVariableSignature> typeVariables,
			AnnotatedType returnType) {
		super(name, annotations, visibility, parameters, typeVariables);

		this.returnType = returnType;
	}

	@Override
	protected MethodSignature<T> withExecutableSignatureData(
			String name,
			Set<Annotation> annotations,
			Visibility visibility,
			List<VariableSignature<?>> parameters,
			List<TypeVariableSignature> typeVariables) {
		return new MethodSignature<>(name, annotations, visibility, parameters, typeVariables, returnType);
	}

	public AnnotatedType getReturnType() {
		return returnType;
	}

	public MethodSignature<?> withReturnType(AnnotatedType type) {
		return new MethodSignature<>(name, annotations, visibility, parameters, typeVariables, type);
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
		StringBuilder builder = new StringBuilder();

		ParameterizedSignature.appendTypeParametersTo(this, builder);
		builder.append(' ').append(getReturnType()).append(' ').append(getName());
		appendParameters(builder);

		return builder.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof MethodSignature<?>))
			return false;

		MethodSignature<?> that = (MethodSignature<?>) obj;

		return super.equals(that) && Objects.equals(this.getReturnType(), that.getReturnType());
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ this.getReturnType().hashCode();
	}
}

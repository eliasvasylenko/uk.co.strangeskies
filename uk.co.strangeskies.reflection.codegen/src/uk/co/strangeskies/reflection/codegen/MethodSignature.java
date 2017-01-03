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

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.reflection.Visibility.forModifiers;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.Visibility;
import uk.co.strangeskies.reflection.token.TypeToken;

public class MethodSignature<T> extends ExecutableSignature<MethodSignature<T>> {
	public static MethodSignature<Void> methodSignature(String methodName) {
		return new MethodSignature<>(methodName);
	}

	public static MethodSignature<?> methodSignature(Method method) {
		return new MethodSignature<>(method.getName())
				.withAnnotations(method.getAnnotations())
				.withVisibility(forModifiers(method.getModifiers()))
				.withReturnType(method.getAnnotatedReturnType())
				.asDefault(method.isDefault())
				.withTypeVariables(
						stream(method.getTypeParameters()).map(TypeVariableSignature::typeVariableSignature).collect(toList()))
				.withParameters(stream(method.getParameters()).map(VariableSignature::variableSignature).collect(toList()));
	}

	private final AnnotatedType returnType;
	private final boolean defaultImplementation;

	protected MethodSignature(String methodName) {
		super(methodName);

		this.returnType = AnnotatedTypes.annotated(void.class);
		this.defaultImplementation = false;
	}

	protected MethodSignature(
			String name,
			Set<Annotation> annotations,
			Visibility visibility,
			List<VariableSignature<?>> parameters,
			List<TypeVariableSignature> typeVariables,
			AnnotatedType returnType,
			boolean defaultImplementation) {
		super(name, annotations, visibility, parameters, typeVariables);

		this.returnType = returnType;
		this.defaultImplementation = defaultImplementation;
	}

	@Override
	protected MethodSignature<T> withExecutableSignatureData(
			String name,
			Set<Annotation> annotations,
			Visibility visibility,
			List<VariableSignature<?>> parameters,
			List<TypeVariableSignature> typeVariables) {
		return new MethodSignature<>(
				name,
				annotations,
				visibility,
				parameters,
				typeVariables,
				returnType,
				defaultImplementation);
	}

	public boolean isDefault() {
		return defaultImplementation;
	}

	public MethodSignature<T> asDefault(boolean defaultImplementation) {
		return new MethodSignature<>(
				name,
				annotations,
				visibility,
				parameters,
				typeVariables,
				returnType,
				defaultImplementation);
	}

	public MethodSignature<T> asDefault() {
		return asDefault(true);
	}

	public AnnotatedType getReturnType() {
		return returnType;
	}

	public MethodSignature<?> withReturnType(AnnotatedType returnType) {
		return new MethodSignature<>(
				name,
				annotations,
				visibility,
				parameters,
				typeVariables,
				returnType,
				defaultImplementation);
	}

	public MethodSignature<?> withReturnType(Type returnType) {
		return withReturnType(AnnotatedTypes.annotated(returnType));
	}

	@SuppressWarnings("unchecked")
	public <U> MethodSignature<U> withReturnType(Class<U> returnType) {
		return (MethodSignature<U>) withReturnType(AnnotatedTypes.annotated(returnType));
	}

	@SuppressWarnings("unchecked")
	public <U> MethodSignature<U> withReturnType(TypeToken<U> returnType) {
		return (MethodSignature<U>) withReturnType(returnType.getAnnotatedDeclaration());
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

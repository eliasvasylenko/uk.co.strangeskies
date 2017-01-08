/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
 * This file is part of uk.co.strangeskies.reflection.codegen.
 *
 * uk.co.strangeskies.reflection.codegen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection.codegen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.codegen;

import static java.util.stream.Collectors.joining;
import static uk.co.strangeskies.reflection.token.TypeToken.overAnnotatedType;
import static uk.co.strangeskies.utilities.collection.StreamUtilities.entriesToMap;

import java.lang.reflect.AnnotatedType;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.TypeToken;

public class MethodDeclaration<C, T> extends ParameterizedDeclaration<ExecutableSignature<?>> {
	enum Kind {
		CONSTRUCTOR, INSTANCE_METHOD, STATIC_METHOD
	}

	private final String name;
	private final Kind kind;

	private final ClassDeclaration<?, ?> declaringClass;
	private final ClassDeclaration<?, C> owningDeclaration;
	private final TypeToken<T> returnType;
	private final Map<ParameterSignature<?>, LocalVariableExpression<?>> parameters;
	private final boolean staticMethod;

	@SuppressWarnings("unchecked")
	protected MethodDeclaration(
			String name,
			Kind kind,
			ClassDeclaration<?, ?> declaringClass,
			ClassDeclaration<?, C> owningDeclaration,
			AnnotatedType returnType,
			ExecutableSignature<?> signature,
			boolean staticMethod) {
		super(signature);

		this.name = name;
		this.kind = kind;
		this.declaringClass = declaringClass;
		this.owningDeclaration = owningDeclaration;
		this.returnType = (TypeToken<T>) overAnnotatedType(substituteTypeVariableSignatures(returnType));
		this.parameters = signature
				.getParameters()
				.map(parameter -> new SimpleEntry<>(parameter, createParameter((ParameterSignature<?>) parameter)))
				.collect(entriesToMap());
		this.staticMethod = staticMethod;

		if (isStatic() && isDefault()) {
			throw new CodeGenerationException(m -> m.staticMethodCannotBeDefault(this));
		}
	}

	protected static <C, T> MethodDeclaration<C, T> declareConstructor(
			ClassDeclaration<C, T> classDeclaration,
			ConstructorSignature signature) {
		return new MethodDeclaration<>(
				classDeclaration.getSignature().getClassName(),
				Kind.CONSTRUCTOR,
				classDeclaration,
				classDeclaration.getEnclosingClass(),
				classDeclaration.asToken().getAnnotatedDeclaration(),
				signature,
				false);
	}

	protected static <C, T> MethodDeclaration<C, T> declareStaticMethod(
			ClassDeclaration<C, ?> classDeclaration,
			MethodSignature<T> signature) {
		return new MethodDeclaration<>(
				signature.getName(),
				Kind.STATIC_METHOD,
				classDeclaration,
				classDeclaration.getEnclosingClass(),
				signature.getReturnType(),
				signature,
				true);
	}

	protected static <C, T> MethodDeclaration<C, T> declareMethod(
			ClassDeclaration<?, C> classDeclaration,
			MethodSignature<T> signature) {
		return new MethodDeclaration<>(
				signature.getName(),
				Kind.INSTANCE_METHOD,
				classDeclaration,
				classDeclaration,
				signature.getReturnType(),
				signature,
				false);
	}

	public ClassDeclaration<?, ?> getDeclaringClass() {
		return declaringClass;
	}

	public ClassDeclaration<?, C> getOwningDeclaration() {
		return owningDeclaration;
	}

	@SuppressWarnings("unchecked")
	public <U> LocalVariableExpression<U> getParameter(ParameterSignature<U> parameterSignature) {
		return (LocalVariableExpression<U>) parameters.get(parameterSignature);
	}

	private <U> LocalVariableExpression<U> createParameter(ParameterSignature<U> parameterSignature) {
		TypeToken<?> typeToken = overAnnotatedType(substituteTypeVariableSignatures(parameterSignature.getType()));

		@SuppressWarnings("unchecked")
		LocalVariableExpression<U> variable = new LocalVariableExpression<>(
				parameterSignature.getVariableName(),
				(TypeToken<U>) typeToken);

		return variable;
	}

	public ExecutableToken<C, T> asToken() {
		return null;
	}

	public String getName() {
		return name;
	}

	public Kind getKind() {
		return kind;
	}

	public TypeToken<T> getReturnType() {
		return returnType;
	}

	public Stream<LocalVariableExpression<?>> getParameters() {
		return parameters.values().stream();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		/*-
		if (isPrivate())
			builder.append("private ");
		else if (isProtected())
			builder.append("protected ");
		else if (isPublic())
			builder.append("public ");
		
		if (isNative())
			builder.append("native ");
		if (isStatic())
			builder.append("static ");
		if (isStrict())
			builder.append("strictfp ");
		if (isSynchronized())
			builder.append("synchronized ");
		
		if (isAbstract())
			builder.append("abstract ");
		else if (isFinal())
			builder.append("final ");
		 */

		if (isParameterized()) {
			builder.append("<").append(getTypeVariables().map(Objects::toString).collect(joining(", "))).append("> ");
		}

		builder.append(returnType).toString();
		if (getKind() != Kind.CONSTRUCTOR)
			builder.append(" ").append(declaringClass).append(".").append(name);

		return builder
				.append("(")
				.append(signature.getParameters().map(Objects::toString).collect(joining(", ")))
				.append(")")
				.toString();
	}

	public boolean isConstructor() {
		return getSignature() instanceof ConstructorSignature;
	}

	public boolean isStatic() {
		return staticMethod;
	}

	public boolean isDefault() {
		return !isConstructor() && ((MethodSignature<?>) getSignature()).getModifiers().isDefault();
	}
}

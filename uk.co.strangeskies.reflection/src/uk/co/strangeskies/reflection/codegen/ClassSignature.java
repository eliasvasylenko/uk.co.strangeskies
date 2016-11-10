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

import static java.util.Collections.emptySet;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.token.TypeToken;

/**
 * Separating the logic for declaring the class into a builder allows us to
 * ensure the type of the class is immutable once an actual
 * {@link ClassDefinition} object is instantiated. This means that the type can
 * be safely reasoned about before any class members are defined or any
 * implementation details are specified.
 * 
 * <p>
 * A class signature contains a number of {@link MethodSignature method
 * signatures}. Some of these signatures may be implied by inheritance, and some
 * may be explicitly attached. When a class signature is declared, each of those
 * signatures is realized into a corresponding {@link MethodDeclaration method
 * declaration}.
 * 
 * @author Elias N Vasylenko
 * 
 * @param <T>
 *          the intersection of the supertypes of the described class
 */
public class ClassSignature<T> extends ParameterizedSignature<ClassSignature<T>> {
	private final String typeName;
	private final Collection<? extends AnnotatedType> superType;
	private final Collection<? extends MethodSignature<?>> methodSignatures;

	public ClassSignature(String typeName) {
		this.typeName = typeName;
		this.superType = emptySet();
		this.methodSignatures = emptySet();
	}

	protected ClassSignature(
			String typeName,
			Collection<? extends AnnotatedType> superType,
			Collection<? extends MethodSignature<?>> methodSignatures,
			Collection<? extends TypeVariableSignature> typeVariables,
			Collection<? extends Annotation> annotations) {
		super(typeVariables, annotations);

		this.typeName = typeName;
		this.superType = superType;
		this.methodSignatures = methodSignatures;
	}

	@Override
	protected ClassSignature<T> withParameterizedDeclarationData(
			Collection<? extends TypeVariableSignature> typeVariables,
			Collection<? extends Annotation> annotations) {
		return new ClassSignature<>(typeName, superType, methodSignatures, typeVariables, annotations);
	}

	protected String getTypeName() {
		return typeName;
	}

	protected Stream<? extends AnnotatedType> getSuperTypes() {
		return superType.stream();
	}

	public Stream<? extends MethodSignature<?>> getMethodSignatures() {
		return methodSignatures.stream();
	}

	/**
	 * @param superType
	 *          the supertype for the class signature
	 * @return the receiver
	 */
	public ClassSignature<?> withSuperType(Type... superType) {
		return withSuperType(Arrays.stream(superType).map(AnnotatedTypes::over).collect(Collectors.toList()));
	}

	/**
	 * @param superType
	 *          the supertype for the class signature
	 * @return the receiver
	 */
	public ClassSignature<?> withSuperType(AnnotatedType... superType) {
		return withSuperType(Arrays.asList(superType));
	}

	/**
	 * @param <U>
	 *          the supertype for the class signature
	 * @param superType
	 *          the supertype for the class signature
	 * @return the receiver
	 */
	public <U extends T> ClassSignature<U> withSuperType(Class<U> superType) {
		return withSuperType(TypeToken.overType(superType));
	}

	/**
	 * @param <U>
	 *          the supertype for the class signature
	 * @param superType
	 *          the supertype for the class signature
	 * @return the receiver
	 */
	@SuppressWarnings("unchecked")
	public <U extends T> ClassSignature<U> withSuperType(TypeToken<U> superType) {
		return (ClassSignature<U>) withSuperType(superType.getAnnotatedDeclaration());
	}

	/**
	 * @param <U>
	 *          the supertype for the class signature
	 * @param superType
	 *          the supertype for the class signature
	 * @return the receiver
	 */
	@SafeVarargs
	@SuppressWarnings("unchecked")
	public final <U extends T> ClassSignature<U> withSuperType(TypeToken<? extends U>... superType) {
		return (ClassSignature<U>) withSuperType(
				Arrays.stream(superType).map(TypeToken::getAnnotatedDeclaration).collect(Collectors.toList()));
	}

	/**
	 * @param superType
	 *          the supertype for the class signature
	 * @return the receiver
	 */
	public ClassSignature<?> withSuperType(Collection<? extends AnnotatedType> superType) {
		return new ClassSignature<>(typeName, superType, methodSignatures, typeVariables, annotations);
	}

	public ClassSignature<? extends T> withMethod(MethodSignature<?> methodSignature) {
		HashSet<MethodSignature<?>> methodSignatures = new HashSet<>(this.methodSignatures);
		methodSignatures.add(methodSignature);

		return new ClassSignature<>(typeName, superType, methodSignatures, typeVariables, annotations);
	}

	public ClassDeclaration<? extends T> declare() {
		return new ClassDeclaration<>(this);
	}
}

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
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
	private final Set<AnnotatedType> superType;

	private final Set<ConstructorSignature> constructorSignatures;
	private final Set<MethodSignature<?>> staticMethodSignatures;
	private final Set<MethodSignature<?>> methodSignatures;

	protected ClassSignature(String typeName) {
		this.typeName = typeName;
		this.superType = emptySet();
		this.constructorSignatures = emptySet();
		this.methodSignatures = emptySet();
		this.staticMethodSignatures = emptySet();
	}

	protected ClassSignature(
			String typeName,
			Set<AnnotatedType> superType,
			Set<ConstructorSignature> constructorSignatures,
			Set<MethodSignature<?>> staticMethodSignatures,
			Set<MethodSignature<?>> methodSignatures,
			List<TypeVariableSignature> typeVariables,
			Set<Annotation> annotations) {
		super(typeVariables, annotations);

		this.typeName = typeName;
		this.superType = superType;
		this.constructorSignatures = constructorSignatures;
		this.methodSignatures = methodSignatures;
		this.staticMethodSignatures = staticMethodSignatures;
	}

	public static ClassSignature<Object> classSignature(String name) {
		return new ClassSignature<>(name);
	}

	@Override
	protected ClassSignature<T> withParameterizedSignatureData(
			List<TypeVariableSignature> typeVariables,
			Set<Annotation> annotations) {
		return new ClassSignature<>(
				typeName,
				superType,
				constructorSignatures,
				staticMethodSignatures,
				methodSignatures,
				typeVariables,
				annotations);
	}

	protected String getTypeName() {
		return typeName;
	}

	protected Stream<? extends AnnotatedType> getSuperTypes() {
		return superType.stream();
	}

	public Stream<? extends ConstructorSignature> getConstructorSignatures() {
		return constructorSignatures.stream();
	}

	public Stream<? extends MethodSignature<?>> getStaticMethodSignatures() {
		return staticMethodSignatures.stream();
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
		return withSuperType(Arrays.stream(superType).map(AnnotatedTypes::annotated).collect(Collectors.toList()));
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
				stream(superType).map(TypeToken::getAnnotatedDeclaration).collect(toList()));
	}

	/**
	 * @param superType
	 *          the supertype for the class signature
	 * @return the receiver
	 */
	public ClassSignature<?> withSuperType(Collection<? extends AnnotatedType> superType) {
		return new ClassSignature<>(
				typeName,
				new HashSet<>(superType),
				constructorSignatures,
				staticMethodSignatures,
				methodSignatures,
				typeVariables,
				annotations);
	}

	public ClassSignature<? extends T> declareConstructor(ConstructorSignature constructorSignature) {
		HashSet<ConstructorSignature> constructorSignatures = new HashSet<>(this.constructorSignatures);
		constructorSignatures.add(constructorSignature);

		return new ClassSignature<>(
				typeName,
				superType,
				new HashSet<>(constructorSignatures),
				staticMethodSignatures,
				methodSignatures,
				typeVariables,
				annotations);
	}

	public ClassSignature<? extends T> declareStaticMethod(MethodSignature<?> methodSignature) {
		HashSet<MethodSignature<?>> staticMethodSignatures = new HashSet<>(this.staticMethodSignatures);
		staticMethodSignatures.add(methodSignature);

		return new ClassSignature<>(
				typeName,
				superType,
				constructorSignatures,
				staticMethodSignatures,
				methodSignatures,
				typeVariables,
				annotations);
	}

	public ClassSignature<? extends T> declareMethod(MethodSignature<?> methodSignature) {
		HashSet<MethodSignature<?>> methodSignatures = new HashSet<>(this.methodSignatures);
		methodSignatures.add(methodSignature);

		return new ClassSignature<>(
				typeName,
				superType,
				constructorSignatures,
				staticMethodSignatures,
				methodSignatures,
				typeVariables,
				annotations);
	}

	public ClassDeclaration<Void, ? extends T> declare() {
		return new ClassDeclaration<>(null, this);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ClassSignature<?>))
			return false;

		ClassSignature<?> that = (ClassSignature<?>) obj;

		return super.equals(that) && Objects.equals(this.typeName, that.typeName)
				&& Objects.equals(this.superType, that.superType)
				&& Objects.equals(this.constructorSignatures, that.constructorSignatures)
				&& Objects.equals(this.staticMethodSignatures, that.staticMethodSignatures)
				&& Objects.equals(this.methodSignatures, that.methodSignatures);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ superType.hashCode() ^ constructorSignatures.hashCode()
				^ staticMethodSignatures.hashCode() ^ methodSignatures.hashCode();
	}
}

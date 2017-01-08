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

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.reflection.codegen.ConstructorSignature.constructorSignature;
import static uk.co.strangeskies.reflection.codegen.MethodSignature.methodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
public class ClassSignature<T> implements ParameterizedSignature<ClassSignature<T>> {
	private final String packageName;
	private final String simpleName;
	private final String enclosingClassName;

	private final Set<AnnotatedType> superType;

	private final Set<ConstructorSignature> constructorSignatures;
	private final Set<MethodSignature<?>> methodSignatures;
	private final List<TypeVariableSignature> typeVariables;
	private final Set<Annotation> annotations;

	protected ClassSignature() {
		this(null, null, null, emptySet(), emptySet(), emptySet(), emptyList(), emptySet());
	}

	protected ClassSignature(
			String packageName,
			String simpleName,
			String enclosingClassName,
			Set<AnnotatedType> superType,
			Set<ConstructorSignature> constructorSignatures,
			Set<MethodSignature<?>> methodSignatures,
			List<TypeVariableSignature> typeVariables,
			Set<Annotation> annotations) {
		this.packageName = packageName;
		this.simpleName = simpleName;
		this.enclosingClassName = enclosingClassName;
		this.superType = superType;
		this.constructorSignatures = constructorSignatures;
		this.methodSignatures = methodSignatures;
		this.typeVariables = typeVariables;
		this.annotations = annotations;
	}

	public static ClassSignature<Object> classSignature() {
		return new ClassSignature<>();
	}

	/**
	 * Create the class signature describing the given class.
	 * 
	 * @param clazz
	 *          the class whose signature we wish to create
	 * @return a signature describing the given class
	 */
	@SuppressWarnings("unchecked")
	public static <T> ClassSignature<T> classSignature(Class<T> clazz) {
		ClassSignature<?> classSignature = new ClassSignature<>()
				.withPackageName(clazz.getPackage().getName())
				.withSimpleName(clazz.getSimpleName())
				.withEnclosingClass(clazz.getEnclosingClass().getName())
				.withAnnotations(clazz.getDeclaredAnnotations())
				.withTypeVariables(
						stream(clazz.getTypeParameters()).map(TypeVariableSignature::typeVariableSignature).collect(toList()));

		List<AnnotatedType> superType = new ArrayList<>(clazz.getInterfaces().length);
		if (clazz.getSuperclass() != null) {
			superType.add(clazz.getAnnotatedSuperclass());
		}
		stream(clazz.getAnnotatedInterfaces()).forEach(superType::add);
		classSignature = classSignature.withSuperType(superType);

		for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
			classSignature = classSignature.withConstructor(constructorSignature(constructor));
		}
		for (Method method : clazz.getDeclaredMethods()) {
			classSignature = classSignature.withMethod(methodSignature(method));
		}

		return (ClassSignature<T>) classSignature;
	}

	protected String getClassName() {
		StringBuilder builder = new StringBuilder();

		getPackageName().ifPresent(p -> builder.append(p).append('.'));

		getEnclosingClassName().ifPresent(e -> builder.append(e).append('.'));

		return builder.append(getSimpleName()).toString();
	}

	protected Optional<String> getPackageName() {
		return Optional.ofNullable(packageName);
	}

	public ClassSignature<T> withPackageName(String packageName) {
		if (packageName.equals("")) {
			packageName = null;
		}

		return new ClassSignature<>(
				packageName,
				simpleName,
				enclosingClassName,
				superType,
				constructorSignatures,
				methodSignatures,
				typeVariables,
				annotations);
	}

	public ClassSignature<T> withPackageName(Function<String, String> packageNameTransformation) {
		return new ClassSignature<>(
				packageNameTransformation.apply(packageName),
				simpleName,
				enclosingClassName,
				superType,
				constructorSignatures,
				methodSignatures,
				typeVariables,
				annotations);
	}

	protected String getSimpleName() {
		return simpleName;
	}

	public ClassSignature<T> withSimpleName(String simpleName) {
		return new ClassSignature<>(
				packageName,
				simpleName,
				enclosingClassName,
				superType,
				constructorSignatures,
				methodSignatures,
				typeVariables,
				annotations);
	}

	public ClassSignature<T> withSimpleName(Function<String, String> simpleNameTransformation) {
		return new ClassSignature<>(
				packageName,
				simpleNameTransformation.apply(simpleName),
				enclosingClassName,
				superType,
				constructorSignatures,
				methodSignatures,
				typeVariables,
				annotations);
	}

	public Optional<String> getEnclosingClassName() {
		return Optional.ofNullable(enclosingClassName);
	}

	public ClassSignature<T> withEnclosingClass(String enclosingClassName) {
		return new ClassSignature<>(
				packageName,
				simpleName,
				enclosingClassName,
				superType,
				constructorSignatures,
				methodSignatures,
				typeVariables,
				annotations);
	}

	protected Stream<? extends AnnotatedType> getSuperTypes() {
		return superType.stream();
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
	public <U> ClassSignature<? extends U> withSuperType(Class<U> superType) {
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
	public <U> ClassSignature<? extends U> withSuperType(TypeToken<U> superType) {
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
	public final <U> ClassSignature<? extends U> withSuperType(TypeToken<? extends U>... superType) {
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
				packageName,
				simpleName,
				enclosingClassName,
				new HashSet<>(superType),
				constructorSignatures,
				methodSignatures,
				typeVariables,
				annotations);
	}

	public Stream<? extends ConstructorSignature> getConstructorSignatures() {
		return constructorSignatures.stream();
	}

	public ClassSignature<T> withConstructor(ConstructorSignature constructorSignature) {
		HashSet<ConstructorSignature> constructorSignatures = new HashSet<>(this.constructorSignatures);
		constructorSignatures.add(constructorSignature);

		return new ClassSignature<>(
				packageName,
				simpleName,
				enclosingClassName,
				superType,
				constructorSignatures,
				methodSignatures,
				typeVariables,
				annotations);
	}

	public Stream<? extends MethodSignature<?>> getMethodSignatures() {
		return methodSignatures.stream();
	}

	public ClassSignature<T> withMethod(MethodSignature<?> methodSignature) {
		HashSet<MethodSignature<?>> methodSignatures = new HashSet<>(this.methodSignatures);
		methodSignatures.add(methodSignature);

		return new ClassSignature<>(
				packageName,
				simpleName,
				enclosingClassName,
				superType,
				constructorSignatures,
				methodSignatures,
				typeVariables,
				annotations);
	}

	@Override
	public Stream<? extends Annotation> getAnnotations() {
		return annotations.stream();
	}

	@Override
	public ClassSignature<T> withAnnotations(Collection<? extends Annotation> annotations) {
		return new ClassSignature<>(
				packageName,
				simpleName,
				enclosingClassName,
				superType,
				constructorSignatures,
				methodSignatures,
				typeVariables,
				new HashSet<>(annotations));
	}

	@Override
	public Stream<? extends TypeVariableSignature> getTypeVariables() {
		return typeVariables.stream();
	}

	@Override
	public ClassSignature<T> withTypeVariables(Collection<? extends TypeVariableSignature> typeVariables) {
		return new ClassSignature<>(
				packageName,
				simpleName,
				enclosingClassName,
				superType,
				constructorSignatures,
				methodSignatures,
				new ArrayList<>(typeVariables),
				annotations);
	}

	public ClassDefinition<Void, T> defineSingle() {
		ClassDefinitionSpace classSpace = new ClassRegister().withClassSignature(this).declare();
		return new ClassDefinition<>(classSpace.getClassDeclaration(this), classSpace);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ClassSignature<?>))
			return false;

		ClassSignature<?> that = (ClassSignature<?>) obj;

		return super.equals(that) && Objects.equals(this.simpleName, that.simpleName)
				&& Objects.equals(this.superType, that.superType)
				&& Objects.equals(this.constructorSignatures, that.constructorSignatures)
				&& Objects.equals(this.methodSignatures, that.methodSignatures);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ superType.hashCode() ^ constructorSignatures.hashCode() ^ methodSignatures.hashCode();
	}
}

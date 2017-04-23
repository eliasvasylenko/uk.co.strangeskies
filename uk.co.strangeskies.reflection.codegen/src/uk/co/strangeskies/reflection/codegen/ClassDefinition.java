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

import static uk.co.strangeskies.collection.stream.StreamUtilities.throwingMerger;
import static uk.co.strangeskies.reflection.codegen.CodeGenerationException.CODEGEN_PROPERTIES;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

import uk.co.strangeskies.reflection.token.MethodMatcher;

/**
 * @author Elias N Vasylenko
 * 
 * @param <T>
 *          the intersection of the supertypes of the described class
 */
public class ClassDefinition<E, T> extends Definition<ClassDeclaration<E, T>> {
	private final String typeName;
	private final ClassDefinitionSpace classSpace;

	protected ClassDefinition(ClassDeclaration<E, T> declaration, ClassDefinitionSpace classSpace) {
		super(declaration);

		this.typeName = declaration.getSignature().getClassName();
		this.classSpace = classSpace;
	}

	/**
	 * @return the fully qualified class name
	 */
	public String getName() {
		return typeName;
	}

	public ClassDefinitionSpace getClassSpace() {
		return classSpace;
	}

	public <U> ClassDefinition<E, T> defineMethod(
			MethodMatcher<?, ? super U> methodMatcher,
			Function<? super MethodDeclaration<T, U>, ? extends Block<? extends U>> methodBodyFunction) {
		@SuppressWarnings("unchecked")
		MethodDeclaration<T, U> methodDeclaration = getDeclaration()
				.methodDeclarations()
				.filter(m -> methodMatcher.match(m.asToken()))
				.reduce(throwingMerger())
				.map(m -> (MethodDeclaration<T, U>) m)
				.orElseThrow(
						() -> new CodeGenerationException(CODEGEN_PROPERTIES.cannotFindMethodOn(null, null)));

		MethodDefinition<T, U> definition = new MethodDefinition<>(methodDeclaration)
				.withBody(methodBodyFunction.apply(methodDeclaration));

		return new ClassDefinition<>(
				getDeclaration(),
				classSpace.withMethodDefinition(methodDeclaration, definition));
	}

	public <U> ClassDefinition<E, T> defineMethod(
			MethodMatcher<?, U> methodDeclaration,
			Block<? extends U> methodBody) {
		return defineMethod(methodDeclaration, d -> methodBody);
	}

	/**
	 * Derive a class definition which delegates to the given method intercepter
	 * object.
	 * <p>
	 * When multiple intercepters are specified for the same class definition,
	 * they will be attempted in the order they are given until one is found which
	 * is able to delegate.
	 * 
	 * @param intercepter
	 *          the intercepter
	 * @return the derived class definition
	 */
	public <U> ClassDefinition<E, T> delegate(MethodDelegation<? super T> intercepter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return getName();
	}

	public ReflectiveInstance<E, T> instantiateReflectively(Object... arguments) {
		return instantiateReflectively(getClass().getClassLoader(), arguments);
	}

	public ReflectiveInstance<E, T> instantiateReflectively(Collection<? extends Object> arguments) {
		return instantiateReflectively(getClass().getClassLoader(), arguments);
	}

	public ReflectiveInstance<E, T> instantiateReflectively(
			ClassLoader classLoader,
			Object... arguments) {
		return instantiateReflectively(classLoader, Arrays.asList(arguments));
	}

	public ReflectiveInstance<E, T> instantiateReflectively(
			ClassLoader classLoader,
			Collection<?> arguments) {
		return ReflectiveInstanceImpl.instantiate(this, classLoader, Arrays.asList(arguments));
	}

	public Class<T> generateClass() {
		throw new UnsupportedOperationException();
	}
}

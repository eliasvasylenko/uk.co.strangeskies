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
import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.token.TypeToken;

/**
 * This type is a placeholder for a {@link TypeVariable} on a
 * {@link GenericDeclaration} produced from a {@link ParameterizedSignature}.
 * 
 * @author Elias N Vasylenko
 */
public class TypeVariableSignature extends AnnotatedSignature<TypeVariableSignature> {
	static class Reference implements Type {
		private final String name;

		public Reference(String name) {
			this.name = name;
		}

		@Override
		public String getTypeName() {
			return name;
		}
	}

	/**
	 * Type variable declarations intended to have bounds on other type variable
	 * declarations within the same {@link ParameterizedSignature parameterized
	 * declaration} may specify those bounds by reference to the name of the other
	 * type variable. This method creates a placeholder type for this purpose,
	 * which will be substituted with the appropriate {@link TypeVariable} when
	 * the parameterized declaration is actualized into its
	 * {@link ParameterizedDeclaration definition}.
	 * 
	 * This is also useful as type variable declarations sometimes need to be
	 * self-referential in their bounds, whether directly or indirectly. Recursive
	 * data structures are difficult to capture naturally through immutable APIs,
	 * but referencing type variable declarations by name rather than by identity
	 * makes this a little simpler.
	 * 
	 * @param name
	 *          the name of the type variable declaration to create a placeholder
	 *          for
	 * @return a placeholder for a type variable of the given name
	 */
	public static Type referenceTypeVariable(String name) {
		return new Reference(name);
	}

	public static TypeVariableSignature declareTypeVariable(String ofName) {
		return new TypeVariableSignature(ofName);
	}

	private final String name;
	private final Collection<? extends AnnotatedType> bounds;

	/**
	 * @param name
	 *          the name of the declared type parameter
	 */
	protected TypeVariableSignature(String name) {
		this.name = name;
		bounds = emptySet();
	}

	protected TypeVariableSignature(
			String name,
			Collection<? extends AnnotatedType> bounds,
			Collection<? extends Annotation> annotations) {
		super(annotations);

		this.name = name;
		this.bounds = bounds;
	}

	@Override
	protected TypeVariableSignature withAnnotatedDeclarationData(Collection<? extends Annotation> annotations) {
		return new TypeVariableSignature(name, bounds, annotations);
	}

	public String getName() {
		return name;
	}

	public Type reference() {
		return referenceTypeVariable(name);
	}

	public Stream<? extends AnnotatedType> getBounds() {
		return bounds.stream();
	}

	public TypeVariableSignature withBounds(AnnotatedType... bounds) {
		return withBounds(asList(bounds));
	}

	public TypeVariableSignature withBounds(Type... bounds) {
		return withBounds(stream(bounds).map(AnnotatedTypes::over).collect(toList()));
	}

	public TypeVariableSignature withBounds(TypeToken<?>... bounds) {
		return withBounds(stream(bounds).map(TypeToken::getAnnotatedDeclaration).collect(toList()));
	}

	public TypeVariableSignature withBounds(Collection<? extends AnnotatedType> bounds) {
		return new TypeVariableSignature(name, bounds, annotations);
	}
}

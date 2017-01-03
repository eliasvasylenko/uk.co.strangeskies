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
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;

import uk.co.strangeskies.reflection.Visibility;

public class ConstructorSignature extends ExecutableSignature<ConstructorSignature> {
	private static final ConstructorSignature EMPTY_SIGNATURE = new ConstructorSignature();
	protected static final String INIT = "<init>";

	public static ConstructorSignature constructorSignature() {
		return EMPTY_SIGNATURE;
	}

	public static ConstructorSignature constructorSignature(Constructor<?> method) {
		return new ConstructorSignature()
				.withAnnotations(method.getAnnotations())
				.withVisibility(forModifiers(method.getModifiers()))
				.withTypeVariables(
						stream(method.getTypeParameters()).map(TypeVariableSignature::typeVariableSignature).collect(toList()))
				.withParameters(stream(method.getParameters()).map(VariableSignature::variableSignature).collect(toList()));
	}

	protected ConstructorSignature() {
		super(INIT);
	}

	protected ConstructorSignature(
			Set<Annotation> annotations,
			Visibility visibility,
			List<VariableSignature<?>> parameters,
			List<TypeVariableSignature> typeVariables) {
		super(INIT, annotations, visibility, parameters, typeVariables);
	}

	@Override
	protected ConstructorSignature withExecutableSignatureData(
			String name,
			Set<Annotation> annotations,
			Visibility visibility,
			List<VariableSignature<?>> parameters,
			List<TypeVariableSignature> typeVariables) {
		return new ConstructorSignature(annotations, visibility, parameters, typeVariables);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		ParameterizedSignature.appendTypeParametersTo(this, builder);
		builder.append(' ').append(getName());
		appendParameters(builder);

		return builder.toString();
	}
}

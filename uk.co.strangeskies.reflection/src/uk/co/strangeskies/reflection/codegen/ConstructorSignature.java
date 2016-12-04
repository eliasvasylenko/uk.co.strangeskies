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

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

public class ConstructorSignature extends ExecutableSignature<ConstructorSignature> {
	private static final ConstructorSignature EMPTY_SIGNATURE = new ConstructorSignature();
	protected static final String INIT = "<init>";

	public static ConstructorSignature constructorSignature() {
		return EMPTY_SIGNATURE;
	}

	protected ConstructorSignature() {}

	protected ConstructorSignature(
			List<VariableSignature<?>> parameters,
			List<TypeVariableSignature> typeVariables,
			Set<Annotation> annotations) {
		super(parameters, typeVariables, annotations);
	}

	@Override
	protected ConstructorSignature withExecutableSignatureData(
			List<VariableSignature<?>> parameters,
			List<TypeVariableSignature> typeVariables,
			Set<Annotation> annotations) {
		return new ConstructorSignature(parameters, typeVariables, annotations);
	}

	@Override
	public String getName() {
		return INIT;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		appendTypeParameters(builder);
		builder.append(' ').append(getName());
		appendParameters(builder);

		return builder.toString();
	}
}

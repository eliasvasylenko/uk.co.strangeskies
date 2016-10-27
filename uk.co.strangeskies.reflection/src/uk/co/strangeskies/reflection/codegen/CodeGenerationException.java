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

import static uk.co.strangeskies.text.properties.PropertyLoader.getDefaultProperties;

import java.util.function.Function;

import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.ReflectionProperties;
import uk.co.strangeskies.text.properties.Localized;

public class CodeGenerationException extends ReflectionException {
	/**
	 * @param message
	 *          a function from the {@link ReflectionProperties} to the exception
	 *          message
	 */
	public CodeGenerationException(Function<CodeGenerationProperties, Localized<String>> message) {
		this(message.apply(getDefaultProperties(CodeGenerationProperties.class)));
	}

	/**
	 * @param message
	 *          a function from the {@link ReflectionProperties} to the exception
	 *          message
	 * @param cause
	 *          the cause of the exception
	 */
	public CodeGenerationException(Function<CodeGenerationProperties, Localized<String>> message, Throwable cause) {
		this(message.apply(getDefaultProperties(CodeGenerationProperties.class)), cause);
	}

	protected CodeGenerationException(Localized<String> message) {
		super(message);
	}

	protected CodeGenerationException(Localized<String> message, Throwable cause) {
		super(message, cause);
	}
}

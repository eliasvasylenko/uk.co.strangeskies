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
 * This file is part of uk.co.strangeskies.text.
 *
 * uk.co.strangeskies.text is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.text is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.text.properties;

import java.util.Locale;

/**
 * Exception relating to localisation.
 * 
 * @author Elias N Vasylenko
 */
public class PropertyLoaderException extends LocalizedRuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * Build with the given developer locale
	 * 
	 * @param message
	 *          the localised message string
	 * @param developerLocale
	 *          the developer's locale
	 * @param cause
	 *          the cause
	 */
	protected PropertyLoaderException(Localized<String> message, Locale developerLocale, Throwable cause) {
		super(message, developerLocale, cause);
	}

	/**
	 * Build with the given developer locale
	 * 
	 * @param message
	 *          the localised message string
	 * @param developerLocale
	 *          the developer's locale
	 */
	protected PropertyLoaderException(Localized<String> message, Locale developerLocale) {
		super(message, developerLocale);
	}

	/**
	 * Build with an English developer locale
	 * 
	 * @param message
	 *          the localised message string
	 * @param cause
	 *          the cause
	 */
	public PropertyLoaderException(Localized<String> message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Build with an English developer locale
	 * 
	 * @param message
	 *          the localised message string
	 */
	public PropertyLoaderException(Localized<String> message) {
		super(message);
	}
}

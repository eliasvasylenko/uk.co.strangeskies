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
 * This file is part of uk.co.strangeskies.osgi.
 *
 * uk.co.strangeskies.osgi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.osgi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.osgi;

import java.util.Locale;

import uk.co.strangeskies.text.properties.Localized;
import uk.co.strangeskies.text.properties.LocalizedRuntimeException;

/**
 * A localized exception class for dealing with general service wiring and
 * provision issues.
 * 
 * @author Elias N Vasylenko
 */
public class ServiceWiringException extends LocalizedRuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * Build with the given developer locale
	 * 
	 * @param message
	 *          the localized message string
	 * @param developerLocale
	 *          the developer's locale
	 * @param cause
	 *          the cause
	 */
	protected ServiceWiringException(Localized<String> message, Locale developerLocale, Throwable cause) {
		super(message, developerLocale, cause);
	}

	/**
	 * Build with the given developer locale
	 * 
	 * @param message
	 *          the localized message string
	 * @param developerLocale
	 *          the developer's locale
	 */
	protected ServiceWiringException(Localized<String> message, Locale developerLocale) {
		super(message, developerLocale);
	}

	/**
	 * Build with an English developer locale
	 * 
	 * @param message
	 *          the localized message string
	 * @param cause
	 *          the cause
	 */
	public ServiceWiringException(Localized<String> message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Build with an English developer locale
	 * 
	 * @param message
	 *          the localized message string
	 */
	public ServiceWiringException(Localized<String> message) {
		super(message);
	}
}

/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.utilities.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.text;

import java.util.Locale;

/**
 * Exception relating to localisation.
 * 
 * @author Elias N Vasylenko
 */
public class LocalizationException extends LocalizedRuntimeException {
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
	protected LocalizationException(LocalizedString message, Locale developerLocale, Throwable cause) {
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
	protected LocalizationException(LocalizedString message, Locale developerLocale) {
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
	public LocalizationException(LocalizedString message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Build with an English developer locale
	 * 
	 * @param message
	 *          the localised message string
	 */
	public LocalizationException(LocalizedString message) {
		super(message);
	}
}

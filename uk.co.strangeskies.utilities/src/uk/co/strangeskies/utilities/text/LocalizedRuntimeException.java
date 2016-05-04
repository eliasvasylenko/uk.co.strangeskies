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
 * A localised runtime exception class. Exception messages are more frequently
 * for developers than for users, so the message returned by
 * {@link #getMessage()} should always be in the developer's language, which is
 * assumed to be English by default. Constructor overloads are provided for
 * developers to specify a different development language.
 * 
 * @author Elias N Vasylenko
 */
public abstract class LocalizedRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private final LocalizedString message;

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
	public LocalizedRuntimeException(LocalizedString message, Locale developerLocale, Throwable cause) {
		super(message.toString(developerLocale), cause);
		this.message = message;
	}

	/**
	 * Build with the given developer locale
	 * 
	 * @param message
	 *          the localized message string
	 * @param developerLocale
	 *          the developer's locale
	 */
	public LocalizedRuntimeException(LocalizedString message, Locale developerLocale) {
		this(message, developerLocale, null);
	}

	/**
	 * Build with an English developer locale
	 * 
	 * @param message
	 *          the localized message string
	 * @param cause
	 *          the cause
	 */
	public LocalizedRuntimeException(LocalizedString message, Throwable cause) {
		this(message, Locale.ENGLISH, cause);
	}

	/**
	 * Build with an English developer locale
	 * 
	 * @param message
	 *          the localized message string
	 */
	public LocalizedRuntimeException(LocalizedString message) {
		this(message, Locale.ENGLISH);
	}

	@Override
	public String getLocalizedMessage() {
		return message.toString();
	}
}

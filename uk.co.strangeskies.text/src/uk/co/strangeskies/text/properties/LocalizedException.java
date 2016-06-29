/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.text.
 *
 * uk.co.strangeskies.text is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.text is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.text.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.text.properties;

import java.util.Locale;

/**
 * A localized exception class. Exception messages are more frequently for
 * developers than for users, so the message returned by {@link #getMessage()}
 * should always be in the developer's language, which is assumed to be English
 * by default. Constructor overloads are provided for developers to specify a
 * different development language.
 * 
 * @author Elias N Vasylenko
 */
public abstract class LocalizedException extends Exception {
	private static final long serialVersionUID = 1L;

	private final Localized<String> message;

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
	protected LocalizedException(Localized<String> message, Locale developerLocale, Throwable cause) {
		super(message.get(developerLocale), cause);
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
	protected LocalizedException(Localized<String> message, Locale developerLocale) {
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
	public LocalizedException(Localized<String> message, Throwable cause) {
		this(message, Locale.ENGLISH, cause);
	}

	/**
	 * Build with an English developer locale
	 * 
	 * @param message
	 *          the localized message string
	 */
	public LocalizedException(Localized<String> message) {
		this(message, Locale.ENGLISH);
	}

	@Override
	public String getLocalizedMessage() {
		return message.toString();
	}
}

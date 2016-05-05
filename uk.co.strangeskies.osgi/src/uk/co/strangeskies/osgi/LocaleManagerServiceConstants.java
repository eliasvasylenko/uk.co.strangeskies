/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.osgi.
 *
 * uk.co.strangeskies.osgi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.osgi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.osgi.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.osgi;

import java.util.Locale;

/**
 * Constants relating to locale management.
 * 
 * @author Elias N Vasylenko
 */
public final class LocaleManagerServiceConstants {
	private LocaleManagerServiceConstants() {}

	/**
	 * Configuration pid for OSGi configuration.
	 */
	public static final String CONFIGURATION_PID = "uk.co.strangeskies.text.locale.manager";
	/**
	 * Key for locale setting string, in the format specified by
	 * {@link Locale#forLanguageTag(String)}.
	 */
	public static final String LOCALE_KEY = "locale";
}

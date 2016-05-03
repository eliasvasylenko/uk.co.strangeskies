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
import java.util.stream.IntStream;

import uk.co.strangeskies.utilities.Observable;

/**
 * A localised string interface which is observable over the string data changes
 * due to updated locale.
 * 
 * @author Elias N Vasylenko
 */
public interface LocalizedString extends Observable<String>, CharSequence {
	/**
	 * @return the current locale of the string
	 */
	Locale locale();

	/**
	 * @return the localised string value according to the current locale
	 */
	@Override
	String toString();

	/**
	 * @param locale
	 *          the locale to translate to
	 * @return the localised string value according to the given locale
	 */
	String toString(Locale locale);

	@Override
	default int length() {
		return toString().length();
	}

	@Override
	default char charAt(int index) {
		return toString().charAt(index);
	}

	@Override
	default IntStream chars() {
		return toString().chars();
	}

	@Override
	default IntStream codePoints() {
		return toString().codePoints();
	}

	@Override
	default CharSequence subSequence(int start, int end) {
		return toString().subSequence(start, end);
	}
}

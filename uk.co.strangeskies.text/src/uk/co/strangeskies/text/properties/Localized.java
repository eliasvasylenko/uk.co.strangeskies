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
import java.util.function.Consumer;

import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.ObservableValue;

/**
 * A localised property interface which is observable over the value changes due
 * to updated locale.
 * 
 * @author Elias N Vasylenko
 * @param <T>
 *          the type of the value
 */
public interface Localized<T> extends ObservableValue<T> {
	/**
	 * @return the current locale of the string
	 */
	ObservableValue<Locale> locale();

	@Override
	T get();

	/**
	 * @param locale
	 *          the locale to translate to
	 * @return the localised string value according to the given locale
	 */
	T get(Locale locale);

	/**
	 * @param value
	 *          the localised value
	 * @param locale
	 *          the locale of the given text
	 * @return a localised string over the given text and locale
	 */
	static <T> Localized<T> forStaticLocale(T value, Locale locale) {
		return new Localized<T>() {
			@Override
			public T get() {
				return value;
			}

			@Override
			public String toString() {
				return value.toString();
			}

			@Override
			public T get(Locale locale) {
				return get();
			}

			@Override
			public boolean removeObserver(Consumer<? super T> observer) {
				return true;
			}

			@Override
			public boolean addObserver(Consumer<? super T> observer) {
				return true;
			}

			@Override
			public ObservableValue<Locale> locale() {
				return ObservableValue.immutable(locale);
			}

			@Override
			public Observable<Change<T>> changes() {
				return Observable.immutable();
			}
		};
	}
}

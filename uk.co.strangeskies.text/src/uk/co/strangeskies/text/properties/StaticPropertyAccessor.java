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

import uk.co.strangeskies.utilities.Observer;

/**
 * A partial implementation for {@link Properties} manually implemented types
 * with static locale.
 * <p>
 * Implementations can use the {@link #localize(String, Object[])} helper method
 * to provide static {@link Localized} instances of the correct locale.
 * 
 * @author Elias N Vasylenko
 *
 * @param <S>
 *          self bound type
 */
public abstract class StaticPropertyAccessor<S extends Properties<S>> implements Properties<S> {
	private final Locale locale;
	private final Class<S> accessorClass;

	/**
	 * @param locale
	 *          the static locale for the text
	 * @param accessorClass
	 *          the class of the property accessor
	 */
	public StaticPropertyAccessor(Locale locale, Class<S> accessorClass) {
		this.locale = locale;
		this.accessorClass = accessorClass;
	}

	@Override
	public Class<S> getAccessorClass() {
		return accessorClass;
	}

	@Override
	public boolean addObserver(Observer<? super S> observer) {
		return true;
	}

	@Override
	public boolean removeObserver(Observer<? super S> observer) {
		return true;
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param string
	 *          the string to create a localized view of
	 * @param arguments
	 *          the arguments to substitute into the string
	 * @return a {@link Localized} instance over the given string and the static
	 *         {@link #getLocale() locale}
	 */
	public Localized<String> localize(String string, Object... arguments) {
		return Localized.forStaticLocale(String.format(string, arguments), locale);
	}
}

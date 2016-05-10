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
import java.util.Objects;
import java.util.function.Function;

import uk.co.strangeskies.utilities.ObservableProperty;
import uk.co.strangeskies.utilities.ObservablePropertyImpl;

/**
 * Management interface over and associate {@link Localizer localiser instance},
 * allowing the locale of that instance to be changed.
 * <p>
 * A locale manager is observable over changes to its locale.
 * 
 * @author Elias N Vasylenko
 */
public interface LocaleManager extends LocaleProvider, ObservableProperty<Locale, Locale> {
	/**
	 * As returned by {@link #getDefaultManager()}.
	 */
	LocaleManager DEFAULT_MANAGER = new DefaultLocaleManagerImpl();

	/**
	 * @param locale
	 *          the new locale
	 */
	default void setLocale(Locale locale) {
		set(locale);
	}

	/**
	 * @return a simple mutable locale manager, with its locale initialised to the
	 *         system default
	 */
	static LocaleManager getManager() {
		return getManager(Locale.getDefault());
	}

	/**
	 * @param locale
	 *          the initial locale
	 * @return a simple mutable locale manager
	 */
	static LocaleManager getManager(Locale locale) {
		return new LocaleManagerImpl(locale);
	}

	/**
	 * Create a locale manager based on the system default locale, as returned by
	 * {@link Locale#getDefault()}. Changes made to this manager will be forwarded
	 * to the system locale setting, and vice versa.
	 * <p>
	 * Unfortunately there is no standard mechanism to listen for changes to the
	 * system locale, so the manager may not stay up to date until it is refreshed
	 * by a call to {@link #setLocale(Locale)} or {@link #getLocale()}.
	 * 
	 * @return a locale manager backed by the system locale
	 */
	static LocaleManager getDefaultManager() {
		return DEFAULT_MANAGER;
	}

}

class LocaleManagerImpl extends ObservablePropertyImpl<Locale, Locale> implements LocaleManager {
	public LocaleManagerImpl(Locale locale) {
		super(Function.identity(), Objects::equals, locale);
	}

	@Override
	public synchronized void setLocale(Locale locale) {
		set(locale);
	}

	@Override
	public synchronized Locale getLocale() {
		return get();
	}
}

class DefaultLocaleManagerImpl extends LocaleManagerImpl {
	public DefaultLocaleManagerImpl() {
		super(Locale.getDefault());
	}

	@Override
	public synchronized void setLocale(Locale locale) {
		Locale.setDefault(locale);
		super.setLocale(locale);
	}

	@Override
	public synchronized Locale getLocale() {
		Locale locale = Locale.getDefault();

		/*
		 * Actual system locale may have changed, since we can't listen to events,
		 * so make sure we're synced here.
		 */
		setLocale(locale);

		return locale;
	}
}

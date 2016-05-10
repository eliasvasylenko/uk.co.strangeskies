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
import java.util.ResourceBundle;

import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.Self;

/**
 * A super-interface for fetching localised text items. Users should not
 * implement this class, instead they should define sub-interfaces, allowing
 * implementations to be automatically provided by the {@link Localizer} class.
 * <p>
 * User provided methods should all return {@link LocalizedString}, which will
 * be provided by the {@link Localizer} and updated when the {@link Locale} is
 * changed.
 * <p>
 * A key is generated for each method based on the class name and the method
 * name, to look up a property from the {@link ResourceBundle} provided by the
 * {@link Localizer} according to
 * {@link Localizer#getKey(java.lang.reflect.Method, Object[])}. The key generation rules
 * can be overridden for a given method by annotating with
 * {@link LocalizationKey}.
 * <p>
 * Default methods will be invoked directly.
 * <p>
 * A {@link LocalizedText} instance is {@link Observable} over changes to its
 * locale, with the instance itself being passed as the message to observers.
 * 
 * @author Elias N Vasylenko
 *
 * @param <S>
 *          self bound type
 */
public interface LocalizedText<S extends LocalizedText<S>> extends Self<S>, Observable<S> {
	/**
	 * @return the current locale of the text
	 */
	Locale getLocale();

	@Override
	default S copy() {
		return getThis();
	}
}

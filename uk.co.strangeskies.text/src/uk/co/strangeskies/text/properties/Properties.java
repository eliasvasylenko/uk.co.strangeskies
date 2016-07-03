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

import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.Self;

/**
 * A super-interface for fetching localised text items. Users should not
 * implement this class, instead they should define sub-interfaces, allowing
 * implementations to be automatically provided by the {@link PropertyLoader}
 * class.
 * <p>
 * User provided methods should all return {@link Localized}, which will be
 * provided by the {@link PropertyLoader} and updated when the {@link Locale} is
 * changed.
 * <p>
 * A key is generated for each method based on the class name and the method
 * name TODO
 * <p>
 * Default methods will be invoked directly.
 * <p>
 * A {@link Properties} instance is {@link Observable} over changes to its
 * locale, with the instance itself being passed as the message to observers.
 * <p>
 * For an example of how to use this interface, users should take a look at the
 * {@link PropertyLoaderProperties} interface.
 * 
 * @author Elias N Vasylenko
 *
 * @param <S>
 *          self bound type
 */
public interface Properties<S extends Properties<S>> extends Self<S>, Observable<S> {
	String PROPERTIES_POSTFIX = "Properties";

	/**
	 * @return the current locale of the text
	 */
	Locale getLocale();

	@Override
	default S copy() {
		return getThis();
	}

	static String getDefaultName(String name) {
		if (name.endsWith(PROPERTIES_POSTFIX) && !name.equals(PROPERTIES_POSTFIX)) {
			name = name.substring(0, name.length() - PROPERTIES_POSTFIX.length());
		}

		return name;
	}
}

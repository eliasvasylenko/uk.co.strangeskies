/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,L__   __||  _ `.        / \     |  \   | |  ,-`__`]  ,-`__`]
 *   ( (_`-`   | |   | | ) |       / . \    | . \  | | / .`  `  / .`  `
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `-`.   / /   \ \  | | \ \| || |   | || +--J
 *  \ \__.` /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,-
 *   `.__.-`   L_|   L_|    L_|/_/       \_\L_|   \__|  `-.__.'  `-.__.]
 *                   __    _         _      __      __
 *                 ,`_ `, | |   _   | |  ,-`__`]  ,`_ `,
 *                ( (_`-` | '-.) |  | | / .`  `  ( (_`-`
 *                 `._ `. | +-. <   | || '--.     `._ `.
 *                _   `. \| |  `-`. | || +--J    _   `. \
 *               \ \__.` /| |    \ \| | \ `.__,-\ \__.` /
 *                `.__.-` L_|    L_|L_|  `-.__.] `.__.-`
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

import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.Self;

/**
 * A base type for property accessor interfaces.
 * <p>
 * A property accessor interface is an interface to provide an API over static
 * properties and localized text and data. Users should not implement this class
 * themselves, instead they should define sub-interfaces, allowing
 * implementations to be automatically provided by {@link PropertyLoader}.
 * <p>
 * User defined methods on a property accessor interface may return values of
 * any type, so long as a {@link PropertyValueProvider} is given to the property
 * loader which supports that type. Special handling is performed for the
 * {@link Localized} type, and for methods returning nested accessor classes.
 * <p>
 * A key is generated for each method based on the class and method name. The
 * key is generated according to the {@link PropertyConfiguration} used to load
 * the {@link Properties} instance.
 * <p>
 * Default and static methods will be invoked directly.
 * <p>
 * A {@link Properties} instance is {@link Observable} over changes to its
 * locale, with the instance itself being passed as the message to observers.
 * <p>
 * For an example of how to use this interface, users may wish to take a look at
 * the {@link PropertyLoaderProperties} interface.
 * 
 * @author Elias N Vasylenko
 *
 * @param <S>
 *          self bound type
 */
public interface Properties<S extends Properties<S>> extends Self<S>, Observable<S> {
	/**
	 * @return the current locale of the text
	 */
	Locale getLocale();

	/**
	 * @return the interface subtype which is implemented
	 */
	Class<S> getAccessorClass();

	@Override
	default S copy() {
		return getThis();
	}

	/**
	 * @param name
	 *          the string to remove the postfix from
	 * @return the given string, with the simple class name {@link Properties}
	 *         removed from the end, if present.
	 */
	static String removePropertiesPostfix(String name) {
		String postfix = Properties.class.getSimpleName();

		if (name.endsWith(postfix) && !name.equals(postfix)) {
			name = name.substring(0, name.length() - postfix.length());
		}

		return name;
	}

	/**
	 * The default name of the accessor, as given by the
	 * {@link #removePropertiesPostfix(String) remove postfix} method invoked on
	 * the {@link #getAccessorClass() accessor class's} simple name.
	 * 
	 * @return the default name
	 */
	default String getDefaultName() {
		return removePropertiesPostfix(getAccessorClass().getSimpleName());
	}
}

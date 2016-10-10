/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. l      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' l   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.eclipse.
 *
 * uk.co.strangeskies.eclipse is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.eclipse is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.eclipse;

import uk.co.strangeskies.text.properties.Localized;
import uk.co.strangeskies.text.properties.Properties;

/**
 * Text resource accessor for Eclipse OSGi utilities
 * 
 * @author Elias N Vasylenko
 */
public interface LocalizationSupplierProperties extends Properties<LocalizationSupplierProperties> {
	/**
	 * @return invalid type was annotated with {@link Localize} for localization
	 *         supplier
	 */
	default Localized<String> illegalInjectionTarget() {
		return illegalInjectionTarget(Localize.class, Properties.class);
	}

	/**
	 * @param localizeClass
	 *          the {@link Localize} class for formatting
	 * @param localizedTextClass
	 *          the {@link Properties} class for formatting
	 * @return invalid type was annotated with {@link Localize} for localization
	 *         supplier
	 */
	Localized<String> illegalInjectionTarget(Class<Localize> localizeClass,
			@SuppressWarnings("rawtypes") Class<Properties> localizedTextClass);

	/**
	 * @return an unexpected error occurred
	 */
	Localized<String> unexpectedError();
}

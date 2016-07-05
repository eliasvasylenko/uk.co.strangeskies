/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.eclipse.
 *
 * uk.co.strangeskies.eclipse is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.eclipse is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.eclipse.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.eclipse;

import uk.co.strangeskies.text.properties.Localized;
import uk.co.strangeskies.text.properties.Properties;

/**
 * Text resource accessor for Eclipse OSGi utilities
 * 
 * @author Elias N Vasylenko
 */
public interface LocalizationSupplierText extends Properties<LocalizationSupplierText> {
	/**
	 * @return invalid type was annotated with {@link Localize} for localisation
	 *         supplier
	 */
	default Localized illegalInjectionTarget() {
		return illegalInjectionTarget(Localize.class, Properties.class);
	}

	/**
	 * @param localizeClass
	 *          the {@link Localize} class for formatting
	 * @param localizedTextClass
	 *          the {@link Properties} class for formatting
	 * @return invalid type was annotated with {@link Localize} for localisation
	 *         supplier
	 */
	Localized illegalInjectionTarget(Class<Localize> localizeClass,
			@SuppressWarnings("rawtypes") Class<Properties> localizedTextClass);

	/**
	 * @return an unexpected error occurred
	 */
	Localized unexpectedError();
}

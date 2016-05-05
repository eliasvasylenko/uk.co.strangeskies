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

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import uk.co.strangeskies.utilities.text.LocalizedString;
import uk.co.strangeskies.utilities.text.LocalizedText;

/**
 * Text resource accessor for Eclipse OSGi utilities
 * 
 * @author Elias N Vasylenko
 */
public interface ObservableServiceSupplierText extends LocalizedText<ObservableServiceSupplierText> {
	/**
	 * @return invalid type was annotated with {@link ObservableService} for
	 *         service collection injection
	 */
	default LocalizedString illegalInjectionTarget() {
		return illegalInjectionTarget(ObservableService.class, ObservableList.class, ObservableSet.class,
				ObservableValue.class);
	}

	/**
	 * @param observableService
	 *          the {@link ObservableService} class for service collection
	 *          injection
	 * @param list
	 *          an observable list in service ranking order
	 * @param set
	 *          an observable set in service ranking order
	 * @param value
	 *          an observable value of the highest ranking service
	 * @return invalid type was annotated with {@link ObservableService} for
	 *         service collection injection
	 */
	@SuppressWarnings("rawtypes")
	LocalizedString illegalInjectionTarget(Class<ObservableService> observableService, Class<ObservableList> list,
			Class<ObservableSet> set, Class<ObservableValue> value);

	/**
	 * @return an unexpected error occurred
	 */
	LocalizedString unexpectedError();
}

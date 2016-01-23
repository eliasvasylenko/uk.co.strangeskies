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

import org.eclipse.e4.core.di.suppliers.ExtendedObjectSupplier;
import org.eclipse.e4.core.di.suppliers.IObjectDescriptor;
import org.eclipse.e4.core.di.suppliers.IRequestor;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

/**
 * A supplier of observable sets, primarily intended to be populated
 * automatically by the OSGi service registry, for consumption by the Eclipse
 * dependency injection framework.
 * 
 * Declarative service implementations should extend this class, passing
 * services to {@link #addItem(Object)} and {@link #removeItem(Object)} as they
 * become available and unavailable via the service registry.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          The type of the elements of the set we wish to supply
 */
public class ObservableSetSupplier<T> extends ExtendedObjectSupplier {
	@SuppressWarnings("unchecked")
	private final ObservableSet<T> items = FXCollections.observableSet();

	@Override
	public ObservableSet<T> get(IObjectDescriptor descriptor, IRequestor requestor, boolean track, boolean group) {
		return FXCollections.unmodifiableObservableSet(items);
	}

	/**
	 * Add an item to the supplier observable set. Intended to be invoked by the
	 * OSGi service registry.
	 * 
	 * @param item
	 *          An item we wish to add to the set.
	 */
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addItem(T item) {
		items.add(item);
	}

	/**
	 * Remove an item from the supplier observable set. Intended to be invoked by
	 * the OSGi service registry.
	 * 
	 * @param item
	 *          An item we wish to remove from the set.
	 */
	public void removeItem(T item) {
		items.remove(item);
	}
}

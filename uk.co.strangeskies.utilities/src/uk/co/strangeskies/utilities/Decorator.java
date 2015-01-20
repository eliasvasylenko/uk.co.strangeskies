/**
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.
 *
 *     uk.co.strangeskies is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     uk.co.strangeskies is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with uk.co.strangeskies.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities;

/**
 * A basic abstract implementation of the decorator pattern. Derived classes
 * should try to implement some common interface with T.
 * 
 * @author Elias N Vasylenko
 * 
 * @param <T>
 */
public abstract class Decorator<T> {
	private final/*  */Property<T, ? super T> componentProperty;

	public Decorator(T component) {
		this.componentProperty = new IdentityProperty<>(component);
	}

	public Decorator(Property<T, ? super T> component) {
		this.componentProperty = component;
	}

	protected final T getComponent() {
		return componentProperty.get();
	}

	protected final Property<T, ? super T> getComponentProperty() {
		return componentProperty;
	}

	@Override
	public String toString() {
		return getComponent().toString();
	}

	@Override
	public boolean equals(Object obj) {
		return getComponent().equals(obj);
	}

	@Override
	public int hashCode() {
		return getComponent().hashCode();
	}
}

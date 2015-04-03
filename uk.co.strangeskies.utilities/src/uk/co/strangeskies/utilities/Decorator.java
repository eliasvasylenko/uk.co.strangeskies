/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.utilities;

import java.util.function.Supplier;

/**
 * A basic abstract implementation of the decorator pattern. Derived classes
 * should try to implement some common interface with T.
 * 
 * @author Elias N Vasylenko
 * 
 * @param <T>
 */
public abstract class Decorator<T> {
	private final/*  */Supplier<T> component;

	/**
	 * Create a decorator for a given instance.
	 * 
	 * @param component
	 */
	public Decorator(T component) {
		this.component = () -> component;
	}

	/**
	 * Create a decorator for a given mutable Property.
	 * 
	 * @param component
	 *          A supplier of components, to be retrieved every time the decorator
	 *          needs a reference to the actual component.
	 */
	public Decorator(Supplier<T> component) {
		this.component = component;
	}

	protected final T getComponent() {
		return component.get();
	}

	protected final Supplier<T> getComponentProperty() {
		return component;
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

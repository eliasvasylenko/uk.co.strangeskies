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
package uk.co.strangeskies.utilities.factory;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public abstract class Configurator<T> implements Factory<T> {
	private boolean complete = false;

	private final Set<Consumer<T>> listeners;

	public Configurator() {
		listeners = new HashSet<>();
	}

	@Override
	public final T create() {
		if (complete)
			throw new StaleBuilderStateException(this);

		T created;
		try {
			created = tryCreate();
		} catch (Exception e) {
			throw new InvalidBuildStateException(this, e);
		}

		listeners.stream().forEach(c -> c.accept(created));

		complete = true;

		return created;
	}

	protected void addResultListener(Consumer<T> listener) {
		listeners.add(listener);
	}

	protected abstract T tryCreate();

	public final boolean isComplete() {
		return complete;
	}

	public final void assertNotStale() {
		if (complete)
			throw new StaleBuilderStateException(this);
	}

	public final void assertComplete() {
		if (!complete)
			throw new IncompleteBuildStateException(this);
	}

	protected void assertConfigurable(Object... objects) {
		if (complete)
			throw new InvalidBuildStateException(this);

		for (Object object : objects)
			if (object != null)
				throw new InvalidBuildStateException(this, "Object '" + object
						+ "' is already configured");
	}

	protected void assertConfigured(Object... objects) {
		for (Object object : objects)
			if (object == null)
				throw new InvalidBuildStateException(this,
						"A dependency object is not yet configured");
	}
}

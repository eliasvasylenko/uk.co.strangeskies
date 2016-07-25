/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A simple implementation of {@link Observable} which maintains a list of
 * listeners to receive events fired with {@link #fire(Object)}.
 * <p>
 * Addition and removal of observers, as well as the firing of events, are
 * synchronized on the implementation object.
 * 
 * @author Elias N Vasylenko
 * @param <M>
 *          The type of event message to produce
 */
public class ObservableImpl<M> implements Observable<M> {
	private Set<Consumer<? super M>> observers;

	@Override
	public boolean addObserver(Consumer<? super M> observer) {
		return getObservers().add(observer);
	}

	@Override
	public boolean removeObserver(Consumer<? super M> observer) {
		if (observers != null) {
			return observers.remove(observer);
		} else {
			return false;
		}
	}

	/**
	 * Remove all observers from forwarding.
	 */
	public void clearObservers() {
		if (observers != null) {
			observers.clear();
		}
	}

	/**
	 * Fire the given message to all observers.
	 * 
	 * @param item
	 *          the message event to send
	 */
	public void fire(M item) {
		if (observers != null) {
			for (Consumer<? super M> listener : new ArrayList<>(observers)) {
				listener.accept(item);
			}
		}
	}

	/**
	 * @return a list of all observers attached to this observable
	 */
	public Set<Consumer<? super M>> getObservers() {
		if (observers == null) {
			observers = new LinkedHashSet<>();
		}

		return observers;
	}
}

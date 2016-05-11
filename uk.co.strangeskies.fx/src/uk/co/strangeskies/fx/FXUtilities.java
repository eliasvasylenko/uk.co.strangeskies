/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.fx.
 *
 * uk.co.strangeskies.fx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.fx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.fx.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.fx;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Function;
import java.util.function.Supplier;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.transformation.TransformationList;
import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingHashMap;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;

/**
 * A collection of general utility methods for working with JavaFX and
 * e(fx)clipse.
 * 
 * @author Elias N Vasylenko
 */
public class FXUtilities {
	private FXUtilities() {}

	/**
	 * Perform the given action on the JavaFX event thread as soon as possible,
	 * returning upon completion. This method is safe whether or not the invoking
	 * thread is the event thread.
	 * 
	 * @param runnable
	 *          The action to execute
	 */
	public static void runNow(Runnable runnable) {
		runNow(() -> {
			runnable.run();
			return null;
		});
	}

	/**
	 * Perform the given action on the JavaFX event thread as soon as possible,
	 * returning the result upon completion. This method is safe whether or not
	 * the invoking thread is the event thread.
	 * 
	 * @param <T>
	 *          The type of value to result from the action
	 * @param runnable
	 *          The action to execute
	 * @return The result of invoking the given supplier
	 */
	public static <T> T runNow(Supplier<T> runnable) {
		if (Platform.isFxApplicationThread()) {
			return runnable.get();
		} else {
			FutureTask<T> task = new FutureTask<>(runnable::get);
			Platform.runLater(task);
			try {
				return task.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}

	public static <T, U> ObservableList<T> map(ObservableList<U> component, Function<U, T> mapper) {
		return new TransformationList<T, U>(component) {
			@Override
			public T get(int index) {
				return mapper.apply(getSource().get(index));
			}

			@Override
			public int size() {
				return getSource().size();
			}

			@Override
			public int getSourceIndex(int index) {
				return index;
			}

			@Override
			protected void sourceChanged(Change<? extends U> change) {
				fireChange(new Change<T>(this) {
					@Override
					public boolean wasAdded() {
						return change.wasAdded();
					}

					@Override
					public boolean wasRemoved() {
						return change.wasRemoved();
					}

					@Override
					public boolean wasReplaced() {
						return change.wasReplaced();
					}

					@Override
					public boolean wasUpdated() {
						return change.wasUpdated();
					}

					@Override
					public boolean wasPermutated() {
						return change.wasPermutated();
					}

					@Override
					public int getPermutation(int i) {
						return change.getPermutation(i);
					}

					@Override
					protected int[] getPermutation() {
						throw new AssertionError();
					}

					@Override
					public List<T> getRemoved() {
						List<T> removed = new ArrayList<>(change.getRemovedSize());
						for (U element : change.getRemoved()) {
							removed.add(mapper.apply(element));
						}
						return removed;
					}

					@Override
					public int getFrom() {
						return change.getFrom();
					}

					@Override
					public int getTo() {
						return change.getTo();
					}

					@Override
					public boolean next() {
						return change.next();
					}

					@Override
					public void reset() {
						change.reset();
					}
				});
			}
		};
	}

	public static <T, U> ObservableSet<T> map(ObservableSet<U> component, Function<U, T> mapper) {
		@SuppressWarnings("unchecked")
		ObservableSet<T> set = FXCollections.observableSet();

		ComputingMap<U, T> map = new ComputingHashMap<>(mapper);

		component.addListener((SetChangeListener<U>) event -> {
			if (event.wasAdded()) {
				set.add(map.putGet(event.getElementAdded()));
			}

			if (event.wasRemoved()) {
				T removed = map.removeGet(event.getElementRemoved());
				if (!map.values().contains(removed)) {
					set.remove(event.getElementRemoved());
				}
			}
		});

		return FXCollections.unmodifiableObservableSet(set);
	}

	public static <T> ObservableList<T> asList(ObservableSet<T> component) {
		ObservableList<T> list = FXCollections.observableArrayList();

		component.addListener((SetChangeListener<T>) event -> {
			if (event.wasAdded()) {
				list.add(event.getElementAdded());
			}

			if (event.wasRemoved()) {
				list.remove(event.getElementRemoved());
			}
		});

		return FXCollections.unmodifiableObservableList(list);
	}

	public static <T> ObservableSet<T> asSet(ObservableList<T> component) {
		@SuppressWarnings("unchecked")
		ObservableSet<T> set = FXCollections.observableSet();

		component.addListener((ListChangeListener<T>) event -> {
			boolean removed = false;

			while (event.next()) {
				if (event.wasAdded()) {
					set.addAll(event.getAddedSubList());
				} else if (event.wasRemoved()) {
					/*
					 * We can't simply remove everything, as the list may still contain
					 * duplicates of those removed duplicates
					 */
					removed = true;
				}
			}

			if (removed) {
				set.retainAll(component);
			}
		});

		return FXCollections.unmodifiableObservableSet(set);
	}

	public static <T> ObservableValue<T> wrap(Observable<T> observable, T initial) {
		ObjectProperty<T> property = new SimpleObjectProperty<>(initial);

		observable.addWeakObserver(property, p -> v -> p.set(v));

		return property;
	}

	public static <T> ObservableValue<T> wrap(uk.co.strangeskies.utilities.ObservableValue<T> observable) {
		return wrap(observable, observable.get());
	}
}

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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Function;
import java.util.function.Supplier;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.transformation.TransformationList;
import javafx.fxml.FXMLLoader;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingHashMap;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;

/**
 * A collection of general utility methods for working with JavaFX and
 * e(fx)clipse.
 * 
 * @author Elias N Vasylenko
 */
public class FXUtilities {
	private static final String CONTROLLER_STRING = "Controller";

	private FXUtilities() {}

	/**
	 * For a given controller instance, load an FXML document into that
	 * controller. The location of the document is found by convention, according
	 * to {@link #getResource(Class)}.
	 * 
	 * @param <T>
	 *          The type of the root node
	 * @param loader
	 *          The FXML loader to use
	 * @param controller
	 *          The controller to load an FXML document into
	 * @return The root JavaFX component associated with the controller
	 */
	public static <T> T loadIntoController(FXMLLoader loader, Object controller) {
		loader.setController(controller);

		return loadRoot(loader, controller.getClass());
	}

	/**
	 * For a given controller instance, load an FXML document into that
	 * controller. The location of the document is found by convention, according
	 * to {@link #getResource(Class, String)}.
	 * 
	 * @param <T>
	 *          The type of the root node
	 * @param loader
	 *          The FXML loader to use
	 * @param controller
	 *          The controller to load an FXML document into
	 * @param resourceName
	 *          The name of the resource file
	 * @return The root JavaFX component associated with the controller
	 */
	public static <T> T loadIntoController(FXMLLoader loader, Object controller, String resourceName) {
		loader.setController(controller);

		return loadRoot(loader, controller.getClass(), resourceName);
	}

	/**
	 * For a given controller class, load an FXML document and create a controller
	 * instance. The location of the document is found by convention, according to
	 * {@link #getResource(Class)}.
	 * 
	 * @param <T>
	 *          The type of the root node
	 * @param loader
	 *          The FXML loader to use
	 * @param controllerClass
	 *          The controller class to load an FXML document for
	 * @return The root JavaFX component associated with the controller
	 */
	public static <T> T loadRoot(FXMLLoader loader, Class<?> controllerClass) {
		loader.setLocation(FXUtilities.getResource(controllerClass));

		return loadRootImpl(loader);
	}

	/**
	 * For a given controller class, load an FXML document and create a controller
	 * instance. The location of the document is found by convention, according to
	 * {@link #getResource(Class, String)}.
	 * 
	 * @param <T>
	 *          The type of the root node
	 * @param loader
	 *          The FXML loader to use
	 * @param controllerClass
	 *          The controller class to load an FXML document for
	 * @param resourceName
	 *          The name of the resource file
	 * @return The root JavaFX component associated with the controller
	 */
	public static <T> T loadRoot(FXMLLoader loader, Class<?> controllerClass, String resourceName) {
		loader.setLocation(FXUtilities.getResource(controllerClass, resourceName));

		return loadRootImpl(loader);
	}

	/**
	 * For a given controller class, load an FXML document and create a controller
	 * instance. The location of the document is found by convention, according to
	 * {@link #getResource(Class)}.
	 * 
	 * @param <T>
	 *          The type of the controller
	 * @param loader
	 *          The FXML loader to use
	 * @param controllerClass
	 *          The controller class to load an FXML document for
	 * @return The loaded controller
	 */
	public static <T> T loadController(FXMLLoader loader, Class<T> controllerClass) {
		loader.setLocation(FXUtilities.getResource(controllerClass));

		return loadControllerImpl(loader);
	}

	/**
	 * For a given controller class, load an FXML document and create a controller
	 * instance. The location of the document is found by convention, according to
	 * {@link #getResource(Class, String)}.
	 * 
	 * @param <T>
	 *          The type of the controller
	 * @param loader
	 *          The FXML loader to use
	 * @param controllerClass
	 *          The controller class to load an FXML document for
	 * @param resourceName
	 *          The name of the resource file
	 * @return The loaded controller
	 */
	public static <T> T loadController(FXMLLoader loader, Class<T> controllerClass, String resourceName) {
		loader.setLocation(FXUtilities.getResource(controllerClass, resourceName));

		return loadControllerImpl(loader);
	}

	private static <T> T loadRootImpl(FXMLLoader loader) {
		T root;

		try {
			root = loader.load();
			loader.setRoot(null);
			loader.setController(null);
			loader.setLocation(null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return root;
	}

	private static <T> T loadControllerImpl(FXMLLoader loader) {
		T controller;

		try {
			loader.load();
			controller = loader.getController();
			loader.setRoot(null);
			loader.setController(null);
			loader.setLocation(null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return controller;
	}

	/**
	 * Find the {@code .fxml} resource associated with a given controller class by
	 * location and naming conventions. The location of the file is assumed to be
	 * the same package as the controller class. The name of the file is
	 * determined according to the convention described by
	 * {@link #getResourceName(Class)}.
	 * 
	 * @param controllerClass
	 *          The controller class whose resource we wish to locate
	 * @return The URL for the resource associated with the given controller
	 *         class.
	 */
	public static URL getResource(Class<?> controllerClass) {
		return getResource(controllerClass, getResourceName(controllerClass));
	}

	/**
	 * Find the name of the {@code .fxml} resource associated with a given
	 * controller class by convention. The name of the file is assumed to be
	 * {@code [classname].fxml}, or if {@code [classname]} takes the form
	 * {@code [classnameprefix]Controller}, the name of the file is assumed to be
	 * {@code [classnameprefix].fxml}.
	 * 
	 * @param controllerClass
	 *          The controller class whose resource we wish to locate
	 * @return The URL for the resource associated with the given controller
	 *         class.
	 */
	public static String getResourceName(Class<?> controllerClass) {
		String resourceName = controllerClass.getSimpleName();

		if (resourceName.endsWith(CONTROLLER_STRING)) {
			resourceName = resourceName.substring(0, resourceName.length() - CONTROLLER_STRING.length());
		}

		return resourceName;
	}

	/**
	 * Find the {@code .fxml} resource for a given controller class by location
	 * conventions. The location of the file is assumed to be the same package as
	 * the controller class.
	 * 
	 * @param controllerClass
	 *          The controller class whose resource we wish to locate
	 * @param resourceName
	 *          The name of the resource file
	 * @return The URL for the resource associated with the given controller
	 *         class.
	 */
	public static URL getResource(Class<?> controllerClass, String resourceName) {
		String resourceLocation = "/" + controllerClass.getPackage().getName().replace('.', '/') + "/" + resourceName
				+ ".fxml";

		return controllerClass.getResource(resourceLocation);
	}

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
}

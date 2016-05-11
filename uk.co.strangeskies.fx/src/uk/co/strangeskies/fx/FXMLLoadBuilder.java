package uk.co.strangeskies.fx;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;

public class FXMLLoadBuilder<C> {
	private static final String CONTROLLER_STRING = "Controller";

	private final FXMLLoader loader;

	private C controller;
	private Class<? extends C> controllerClass;
	private Object root;
	private String resourceName;

	/**
	 * @param loader
	 *          The FXML loader to use
	 */
	public FXMLLoadBuilder(FXMLLoader loader) {
		this.loader = loader;
	}

	public static FXMLLoadBuilder<Object> with(FXMLLoader loader) {
		return new FXMLLoadBuilder<>(loader);
	}

	@SuppressWarnings("unchecked")
	public <D extends C> FXMLLoadBuilder<D> controller(D controller) {
		this.controller = controller;
		controllerClass = (Class<? extends C>) controller.getClass();

		return (FXMLLoadBuilder<D>) this;
	}

	@SuppressWarnings("unchecked")
	public <D extends C> FXMLLoadBuilder<D> controller(Class<D> controllerClass) {
		this.controllerClass = controllerClass;
		controller = null;

		return (FXMLLoadBuilder<D>) this;
	}

	public FXMLLoadBuilder<C> resource(String resourceName) {
		this.resourceName = resourceName;

		return this;
	}

	public FXMLLoadBuilder<C> root(Object root) {
		this.root = root;

		return this;
	}

	public <D extends C> FXMLLoadBuilder<D> object(D object) {
		return controller(object).root(object);
	}

	@SuppressWarnings("unchecked")
	public <T> T loadRoot() {
		load();

		return (T) root;
	}

	public C loadController() {
		load();

		return controller;
	}

	public void load() {
		if (controller != null) {
			loader.setController(controller);
		}

		if (resourceName != null) {
			loader.setLocation(getResource(controllerClass, resourceName));
		} else {
			loader.setLocation(getResource(controllerClass));
		}

		if (root != null) {
			loader.setRoot(root);
		}

		try {
			loader.load();
			controller = loader.getController();
			root = loader.getRoot();
			loader.setRoot(null);
			loader.setController(null);
			loader.setLocation(null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
}

package uk.co.strangeskies.fx;

import static uk.co.strangeskies.fx.FXUtilities.getResource;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;

public class FXMLLoadBuilder<C> {
	private final FXMLLoader loader;

	private C controller;
	private Class<? extends C> controllerClass;
	private Object root;
	private URL resource;
	private String resourceName;

	/**
	 * @param loader
	 *          The FXML loader to use
	 */
	public FXMLLoadBuilder(FXMLLoader loader) {
		this.loader = loader;
	}

	public static FXMLLoadBuilder<Object> buildWith(FXMLLoader loader) {
		return new FXMLLoadBuilder<>(loader);
	}

	public static FXMLLoadBuilder<Object> build() {
		return buildWith(new FXMLLoader());
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
		resource = null;

		return this;
	}

	public FXMLLoadBuilder<C> resource(URL resource) {
		this.resource = resource;
		this.resourceName = null;

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
		// set controller
		if (controller != null) {
			loader.setController(controller);
		}

		// set root
		if (root != null) {
			loader.setRoot(root);
		}

		// set location
		if (resource == null) {
			if (resourceName != null) {
				resource = getResource(controllerClass, resourceName);
			} else {
				resource = getResource(controllerClass);
			}
		}
		loader.setLocation(resource);

		// load
		try {
			loader.load();
			controller = loader.getController();
			root = loader.getRoot();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			loader.setRoot(null);
			loader.setController(null);
			loader.setLocation(null);
		}
	}
}

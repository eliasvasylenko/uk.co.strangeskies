package uk.co.strangeskies.gears.utilities;

/**
 * A basic abstract implementation of the decorator pattern. Derived classes
 * should try to implement some common interface with T.
 * 
 * @author Elias N Vasylenko
 * 
 * @param <T>
 */
public abstract class Decorator<T> {
	private final/* @ReadOnly */Property<T, ? super T> componentProperty;

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
}

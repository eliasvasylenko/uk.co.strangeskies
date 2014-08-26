package uk.co.strangeskies.gears.utilities.collection;

import java.util.Set;

import uk.co.strangeskies.gears.utilities.Property;

public class SetDecorator<T> extends CollectionDecorator<Set<T>, T> implements
		Set<T> {
	public SetDecorator(Set<T> component) {
		super(component);
	}

	public SetDecorator(Property<Set<T>, ? super Set<T>> component) {
		super(component);
	}
}

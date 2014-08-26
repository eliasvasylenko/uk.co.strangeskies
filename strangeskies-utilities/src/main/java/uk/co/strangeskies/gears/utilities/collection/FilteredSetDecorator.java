package uk.co.strangeskies.gears.utilities.collection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import uk.co.strangeskies.gears.utilities.Property;

public class FilteredSetDecorator<E> extends SetDecorator<E> {
	public interface Filter<E> {
		public boolean filter(E element);
	}

	private final Filter<E> filter;

	public FilteredSetDecorator(Filter<E> filter) {
		super(new HashSet<E>());

		this.filter = filter;
	}

	public FilteredSetDecorator(Set<E> component, Filter<E> filter) {
		super(component);

		this.filter = filter;
	}

	public FilteredSetDecorator(Property<Set<E>, ? super Set<E>> component,
			Filter<E> filter) {
		super(component);

		this.filter = filter;
	}

	@Override
	public boolean add(E e) {
		return filter.filter(e) && super.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean changed = false;

		for (E e : c)
			changed = add(e) || changed;

		return changed;
	}
}

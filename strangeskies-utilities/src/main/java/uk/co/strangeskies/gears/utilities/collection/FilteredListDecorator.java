package uk.co.strangeskies.gears.utilities.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.co.strangeskies.gears.utilities.Property;

public class FilteredListDecorator<E> extends ListDecorator<E> {
	public interface Filter<E> {
		public boolean filter(E element);
	}

	private final Filter<E> filter;

	public FilteredListDecorator(Filter<E> filter) {
		super(new ArrayList<E>());

		this.filter = filter;
	}

	public FilteredListDecorator(List<E> component, Filter<E> filter) {
		super(component);

		this.filter = filter;
	}

	public FilteredListDecorator(Property<List<E>, ? super List<E>> component,
			Filter<E> filter) {
		super(component);

		this.filter = filter;
	}

	@Override
	public boolean add(E e) {
		return filter.filter(e) && super.add(e);
	}

	@Override
	public void add(int index, E element) {
		if (filter.filter(element))
			super.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean changed = false;

		for (E e : c)
			changed = add(e) || changed;

		return changed;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		for (E e : c)
			add(index++, e);

		return true;
	}
}

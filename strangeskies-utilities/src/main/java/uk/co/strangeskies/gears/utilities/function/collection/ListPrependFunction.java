package uk.co.strangeskies.gears.utilities.function.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ListPrependFunction<E> implements Function<E, List<E>> {
	@Override
	public List<E> apply(E input) {
		List<E> list = new ArrayList<>();
		list.add(input);
		return list;
	}
}

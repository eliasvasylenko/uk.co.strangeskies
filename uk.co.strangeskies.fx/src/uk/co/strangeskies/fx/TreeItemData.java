package uk.co.strangeskies.fx;

import java.util.List;
import java.util.Optional;

import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypedObject;

public interface TreeItemData<T> {
	TypedObject<T> typedData();

	default T data() {
		return typedData().getObject();
	}

	default TypeToken<?> type() {
		return typedData().getType();
	}

	Optional<TreeItemData<?>> parent();

	/**
	 * Get all the contributions which should be applied to a tree item, in order
	 * from most to least specific.
	 * 
	 * @return the contributions which apply to this tree item
	 */
	List<TreeContribution<? super T>> contributions();

	<U extends TreeContribution<? super T>> List<U> contributions(TypeToken<U> type);
}

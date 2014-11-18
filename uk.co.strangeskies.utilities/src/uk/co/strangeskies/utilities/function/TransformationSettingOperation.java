package uk.co.strangeskies.utilities.function;

import java.util.function.Function;

import uk.co.strangeskies.utilities.Property;

public interface TransformationSettingOperation<T extends Property<?, ? super C>, C, F>
		extends AssignmentOperation<T, F> {
	public Function<? super F, ? extends C> getFunction();

	@Override
	public default void assign(T assignee, F assignment) {
		assignee.set(getFunction().apply(assignment));
	}
}

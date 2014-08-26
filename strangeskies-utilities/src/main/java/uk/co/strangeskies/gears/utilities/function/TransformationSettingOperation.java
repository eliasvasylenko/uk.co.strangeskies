package uk.co.strangeskies.gears.utilities.function;

import java.util.function.Function;

import uk.co.strangeskies.gears.utilities.Property;

public class TransformationSettingOperation<T extends Property<?, ? super C>, C, F>
		extends AssignmentOperation<T, F> {
	private final Function<? super F, ? extends C> function;

	public TransformationSettingOperation(
			Function<? super F, ? extends C> function) {
		this.function = function;
	}

	@Override
	public void assign(T assignee, F assignment) {
		assignee.set(function.apply(assignment));
	}
}

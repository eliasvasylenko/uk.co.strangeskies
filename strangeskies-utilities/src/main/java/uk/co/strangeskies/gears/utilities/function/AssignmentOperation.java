package uk.co.strangeskies.gears.utilities.function;

import java.util.function.BiFunction;

public abstract class AssignmentOperation<A, O> implements BiFunction<A, O, A> {
	public abstract void assign(A assignee, O assignment);

	@Override
	public final A apply(A assignee, O assignment) {
		assign(assignee, assignment);

		return assignee;
	}
}

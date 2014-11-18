package uk.co.strangeskies.utilities.function;

import java.util.function.BiFunction;

public interface AssignmentOperation<A, O> extends BiFunction<A, O, A> {
	public abstract void assign(A assignee, O assignment);

	@Override
	public default A apply(A assignee, O assignment) {
		if (assignment == null) {
			throw new IllegalArgumentException(new NullPointerException());
		}

		assign(assignee, assignment);

		return assignee;
	}
}

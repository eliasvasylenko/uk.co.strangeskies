package uk.co.strangeskies.reflection;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

public interface BoundVisitor {
	public static abstract class PartialBoundVisitor implements BoundVisitor {
		@Override
		public void acceptEquality(InferenceVariable<?> a, InferenceVariable<?> b) {}

		@Override
		public void acceptEquality(InferenceVariable<?> a, Type b) {}

		@Override
		public void acceptSubtype(InferenceVariable<?> a, InferenceVariable<?> b) {}

		@Override
		public void acceptSubtype(InferenceVariable<?> a, Type b) {}

		@Override
		public void acceptSubtype(Type a, InferenceVariable<?> b) {}

		@Override
		public void acceptFalsehood() {}

		@Override
		public void acceptCaptureConversion(Map<Type, InferenceVariable<?>> c) {}
	}

	void acceptEquality(InferenceVariable<?> a, InferenceVariable<?> b);

	void acceptEquality(InferenceVariable<?> a, Type b);

	void acceptSubtype(InferenceVariable<?> a, InferenceVariable<?> b);

	void acceptSubtype(InferenceVariable<?> a, Type b);

	void acceptSubtype(Type a, InferenceVariable<?> b);

	void acceptFalsehood();

	void acceptCaptureConversion(Map<Type, InferenceVariable<?>> c);

	public default void visit(Bound... bounds) {
		visit(Arrays.asList(bounds));
	}

	default void visit(Iterable<? extends Bound> bounds) {
		for (Bound bound : bounds)
			bound.accept(this);
	}
}

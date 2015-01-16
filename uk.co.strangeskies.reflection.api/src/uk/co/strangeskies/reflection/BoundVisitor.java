package uk.co.strangeskies.reflection;

import java.lang.reflect.Type;

public interface BoundVisitor {
	public static abstract class PartialBoundVisitor implements BoundVisitor {
		@Override
		public void acceptEquality(InferenceVariable a, InferenceVariable b) {}

		@Override
		public void acceptEquality(InferenceVariable a, Type b) {}

		@Override
		public void acceptSubtype(InferenceVariable a, InferenceVariable b) {}

		@Override
		public void acceptSubtype(InferenceVariable a, Type b) {}

		@Override
		public void acceptSubtype(Type a, InferenceVariable b) {}

		@Override
		public void acceptFalsehood() {}

		@Override
		public void acceptCaptureConversion(CaptureConversion c) {}
	}

	void acceptEquality(InferenceVariable a, InferenceVariable b);

	void acceptEquality(InferenceVariable a, Type b);

	void acceptSubtype(InferenceVariable a, InferenceVariable b);

	void acceptSubtype(InferenceVariable a, Type b);

	void acceptSubtype(Type a, InferenceVariable b);

	void acceptFalsehood();

	void acceptCaptureConversion(CaptureConversion c);

	public default void visit(BoundSet bounds) {
		bounds.stream().forEach(b -> b.accept(this));
	}
}

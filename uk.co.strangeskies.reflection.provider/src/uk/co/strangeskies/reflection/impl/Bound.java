package uk.co.strangeskies.reflection.impl;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import uk.co.strangeskies.utilities.IdentityProperty;

public class Bound {
	public static interface BoundVisitor {
		void acceptEquality(InferenceVariable a, InferenceVariable b);

		void acceptEquality(InferenceVariable a, Type b);

		void acceptSubtype(InferenceVariable a, InferenceVariable b);

		void acceptSubtype(InferenceVariable a, Type b);

		void acceptSubtype(Type a, InferenceVariable b);

		void acceptFalsehood();

		void acceptCaptureConversion(Map<Type, TypeVariable<?>> c);
	}

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
		public void acceptCaptureConversion(Map<Type, TypeVariable<?>> c) {}
	}

	private final Consumer<BoundVisitor> visitation;

	public Bound(Consumer<BoundVisitor> visitation) {
		this.visitation = visitation;
	}

	public void accept(BoundVisitor visitor) {
		visitation.accept(visitor);
	}

	@Override
	public String toString() {
		IdentityProperty<String> result = new IdentityProperty<>();

		accept(new BoundVisitor() {
			@Override
			public void acceptSubtype(Type a, InferenceVariable b) {
				result.set(a + " <: " + b);
			}

			@Override
			public void acceptSubtype(InferenceVariable a, Type b) {
				result.set(a + " <: " + b);
			}

			@Override
			public void acceptSubtype(InferenceVariable a, InferenceVariable b) {
				result.set(a + " <: " + b);
			}

			@Override
			public void acceptFalsehood() {
				result.set("false");
			}

			@Override
			public void acceptEquality(InferenceVariable a, Type b) {
				result.set(a + " = " + b);
			}

			@Override
			public void acceptEquality(InferenceVariable a, InferenceVariable b) {
				result.set(a + " = " + b);
			}

			@Override
			public void acceptCaptureConversion(Map<Type, TypeVariable<?>> c) {
				List<Type> types = new ArrayList<>(c.keySet());
				StringBuilder stringBuilder = new StringBuilder();

				stringBuilder.append("G<");
				stringBuilder.append(types.stream().map(c::get).map(Object::toString)
						.collect(Collectors.joining(", ")));
				stringBuilder.append("> = capture(<G");
				stringBuilder.append(types.stream().map(Object::toString)
						.collect(Collectors.joining(", ")));
				stringBuilder.append(">)");

				result.set(stringBuilder.toString());
			}
		});
		if (result.get() == null)
			throw new AssertionError("Type of bound not understood");

		return result.get();
	}

	@Override
	public boolean equals(Object object) {
		if (object == this)
			return true;
		if (object == null || !(object instanceof Bound))
			return false;

		Bound that = (Bound) object;
		IdentityProperty<Boolean> result = new IdentityProperty<>(false);

		accept(new BoundVisitor() {
			@Override
			public void acceptSubtype(Type a, InferenceVariable b) {
				that.accept(new PartialBoundVisitor() {
					@Override
					public void acceptSubtype(Type a2, InferenceVariable b2) {
						result.set(a.equals(a2) && b.equals(b2));
					}
				});
			}

			@Override
			public void acceptSubtype(InferenceVariable a, Type b) {
				that.accept(new PartialBoundVisitor() {
					@Override
					public void acceptSubtype(InferenceVariable a2, Type b2) {
						result.set(a.equals(a2) && b.equals(b2));
					}
				});
			}

			@Override
			public void acceptSubtype(InferenceVariable a, InferenceVariable b) {
				that.accept(new PartialBoundVisitor() {
					@Override
					public void acceptSubtype(InferenceVariable a2, InferenceVariable b2) {
						result.set(a.equals(a2) && b.equals(b2));
					}
				});
			}

			@Override
			public void acceptFalsehood() {
				that.accept(new PartialBoundVisitor() {
					@Override
					public void acceptFalsehood() {
						result.set(true);
					}
				});
			}

			@Override
			public void acceptEquality(InferenceVariable a, Type b) {
				that.accept(new PartialBoundVisitor() {
					@Override
					public void acceptSubtype(InferenceVariable a2, Type b2) {
						result.set(a.equals(a2) && b.equals(b2));
					}
				});
			}

			@Override
			public void acceptEquality(InferenceVariable a, InferenceVariable b) {
				that.accept(new PartialBoundVisitor() {
					@Override
					public void acceptSubtype(InferenceVariable a2, InferenceVariable b2) {
						result.set(a.equals(a2) && b.equals(b2));
					}
				});
			}

			@Override
			public void acceptCaptureConversion(Map<Type, TypeVariable<?>> c) {
				that.accept(new PartialBoundVisitor() {
					@Override
					public void acceptCaptureConversion(Map<Type, TypeVariable<?>> c2) {
						result.set(c.equals(c2));
					}
				});
			}
		});

		return result.get();
	}

	@Override
	public int hashCode() {
		return 0;
	}
}

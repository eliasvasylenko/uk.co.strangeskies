/**
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.
 *
 *     uk.co.strangeskies is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     uk.co.strangeskies is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with uk.co.strangeskies.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

import java.lang.reflect.Type;
import java.util.function.Consumer;

import uk.co.strangeskies.utilities.IdentityProperty;

public class Bound {
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
				result.set(a.getTypeName() + " <: " + b.getTypeName());
			}

			@Override
			public void acceptSubtype(InferenceVariable a, Type b) {
				result.set(a.getTypeName() + " <: " + b.getTypeName());
			}

			@Override
			public void acceptSubtype(InferenceVariable a, InferenceVariable b) {
				result.set(a.getTypeName() + " <: " + b.getTypeName());
			}

			@Override
			public void acceptFalsehood() {
				result.set("false");
			}

			@Override
			public void acceptEquality(InferenceVariable a, Type b) {
				result.set(a.getTypeName() + " = " + b.getTypeName());
			}

			@Override
			public void acceptEquality(InferenceVariable a, InferenceVariable b) {
				result.set(a.getTypeName() + " = " + b.getTypeName());
			}

			@Override
			public void acceptCaptureConversion(CaptureConversion c) {
				result.set(c.toString());
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
					public void acceptEquality(InferenceVariable a2, Type b2) {
						result.set(a.equals(a2) && b.equals(b2));
					}
				});
			}

			@Override
			public void acceptEquality(InferenceVariable a, InferenceVariable b) {
				that.accept(new PartialBoundVisitor() {
					@Override
					public void acceptEquality(InferenceVariable a2, InferenceVariable b2) {
						result.set((a.equals(a2) && b.equals(b2))
								|| (a.equals(b2) && b.equals(a2)));
					}
				});
			}

			@Override
			public void acceptCaptureConversion(CaptureConversion c) {
				that.accept(new PartialBoundVisitor() {
					@Override
					public void acceptCaptureConversion(CaptureConversion c2) {
						result.set(c.equals(c2));
					}
				});
			}
		});

		return result.get();
	}

	@Override
	public int hashCode() {
		IdentityProperty<Integer> result = new IdentityProperty<>();

		accept(new BoundVisitor() {
			@Override
			public void acceptEquality(InferenceVariable a, InferenceVariable b) {
				result.set(a.hashCode() ^ b.hashCode());
			}

			@Override
			public void acceptEquality(InferenceVariable a, Type b) {
				result.set(a.hashCode() ^ b.hashCode() * 7);
			}

			@Override
			public void acceptSubtype(InferenceVariable a, InferenceVariable b) {
				result.set(a.hashCode() ^ b.hashCode() * 23);
			}

			@Override
			public void acceptSubtype(InferenceVariable a, Type b) {
				result.set(a.hashCode() ^ b.hashCode() * 53);
			}

			@Override
			public void acceptSubtype(Type a, InferenceVariable b) {
				result.set(a.hashCode() ^ b.hashCode() * 67);
			}

			@Override
			public void acceptFalsehood() {
				result.set(0);
			}

			@Override
			public void acceptCaptureConversion(CaptureConversion c) {
				result.set(c.hashCode());
			}
		});

		return result.get();
	}
}

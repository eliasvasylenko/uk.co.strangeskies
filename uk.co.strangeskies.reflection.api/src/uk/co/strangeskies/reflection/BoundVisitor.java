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

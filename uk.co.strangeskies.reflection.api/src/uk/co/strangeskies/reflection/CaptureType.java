/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.reflection.api.
 *
 * uk.co.strangeskies.reflection.api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection.api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.reflection.api.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

abstract class CaptureType implements Type {
	private final static AtomicLong COUNTER = new AtomicLong();

	private final String name;

	private final Type[] upperBounds;
	private final Type[] lowerBounds;

	protected CaptureType(Type[] upperBounds, Type[] lowerBounds) {
		this("CAP", upperBounds, lowerBounds);
	}

	protected CaptureType(String name, Type[] upperBounds, Type[] lowerBounds) {
		this.name = name + "#" + COUNTER.incrementAndGet();

		this.upperBounds = upperBounds.clone();
		this.lowerBounds = lowerBounds.clone();

		if (lowerBounds.length > 0
				&& !Types.isAssignable(IntersectionType.uncheckedOf(lowerBounds),
						IntersectionType.of(upperBounds)))
			throw new TypeInferenceException("Bounds on capture '" + this
					+ "' are invalid. (" + Arrays.toString(lowerBounds) + " <: "
					+ Arrays.toString(upperBounds) + ")");
	}

	public String getName() {
		return name;
	}

	@Override
	public String getTypeName() {
		return getName();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder().append(getName());

		if (upperBounds.length > 0
				&& !(upperBounds.length == 1 && upperBounds[0] == null))
			builder.append(" extends ").append(IntersectionType.of(upperBounds));

		if (lowerBounds.length > 0
				&& !(lowerBounds.length == 1 && lowerBounds[0] == null))
			builder.append(" super ").append(
					IntersectionType.uncheckedOf(lowerBounds));

		return builder.toString();
	}

	public Type[] getUpperBounds() {
		return upperBounds.clone();
	}

	public Type[] getLowerBounds() {
		return lowerBounds.clone();
	}

	protected static <T extends Type, C extends CaptureType> Map<T, C> capture(
			Collection<? extends T> types, Function<T, C> captureFunction) {
		Map<T, C> captures = types.stream()
				.collect(
						Collectors.<T, T, C> toMap(Function.identity(),
								captureFunction::apply));

		substituteBounds(captures);

		return captures;
	}

	private static <T extends Type> void substituteBounds(
			Map<T, ? extends CaptureType> captures) {
		TypeSubstitution substitution = new TypeSubstitution();
		for (T type : captures.keySet())
			substitution = substitution.where(type, captures.get(type));

		for (T type : captures.keySet()) {
			CaptureType capture = captures.get(type);

			for (int i = 0; i < capture.upperBounds.length; i++)
				capture.upperBounds[i] = substitution.resolve(capture.upperBounds[i]);

			for (int i = 0; i < capture.lowerBounds.length; i++)
				capture.lowerBounds[i] = substitution.resolve(capture.lowerBounds[i]);
		}
	}
}

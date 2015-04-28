/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.reflection.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A collection of utility methods relating to wildcard types.
 * 
 * @author Elias N Vasylenko
 */
public class WildcardTypes {
	private WildcardTypes() {}

	/**
	 * Create an unbounded wildcard type.
	 * 
	 * @return An instance of {@link WildcardType} representing an unbounded
	 *         wildcard.
	 */
	public static WildcardType unbounded() {
		return new WildcardType() {
			@Override
			public Type[] getUpperBounds() {
				return new Type[0];
			}

			@Override
			public Type[] getLowerBounds() {
				return new Type[0];
			}

			@Override
			public String toString() {
				return "?";
			}

			@Override
			public boolean equals(Object that) {
				if (!(that instanceof WildcardType))
					return false;
				if (that == this)
					return true;

				WildcardType wildcard = (WildcardType) that;

				return wildcard.getLowerBounds().length == 0
						&& wildcard.getUpperBounds().length == 0
						|| (Arrays.equals(wildcard.getUpperBounds(),
								new Type[] { Object.class }));
			}

			@Override
			public int hashCode() {
				return Arrays.hashCode(getLowerBounds())
						^ Arrays.hashCode(getUpperBounds());
			}
		};
	}

	/**
	 * Create an lower bounded wildcard type.
	 * 
	 * @param type
	 *          The type we wish to be a lower bound for a wildcard.
	 * @return An instance of {@link WildcardType} representing a wildcard with
	 *         the given lower bound.
	 */
	public static WildcardType lowerBounded(Type type) {
		Supplier<Type[]> types;

		if (type instanceof WildcardType) {
			WildcardType wildcardType = ((WildcardType) type);
			if (wildcardType.getUpperBounds().length == 0)
				types = () -> new Type[] { Object.class };
			else
				types = () -> wildcardType.getUpperBounds();
		} else if (type instanceof IntersectionType)
			types = ((IntersectionType) type)::getTypes;
		else
			types = () -> new Type[] { type };

		return new WildcardType() {
			@Override
			public Type[] getUpperBounds() {
				return new Type[0];
			}

			@Override
			public Type[] getLowerBounds() {
				return types.get();
			}

			@Override
			public String toString() {
				return "? super "
						+ Arrays.stream(types.get()).map(Types::toString)
								.collect(Collectors.joining(" & "));
			}

			@Override
			public boolean equals(Object that) {
				if (!(that instanceof WildcardType))
					return false;
				if (that == this)
					return true;
				WildcardType wildcard = (WildcardType) that;
				return Arrays.equals(types.get(), wildcard.getLowerBounds())
						&& (wildcard.getUpperBounds().length == 0 || (Arrays.equals(
								wildcard.getUpperBounds(), new Type[] { Object.class })));
			}

			@Override
			public int hashCode() {
				return Arrays.hashCode(getLowerBounds())
						^ Arrays.hashCode(getUpperBounds());
			}
		};
	}

	/**
	 * Create an upper bounded wildcard type.
	 * 
	 * @param type
	 *          The type we wish to be a upper bound for a wildcard.
	 * @return An instance of {@link WildcardType} representing a wildcard with
	 *         the given upper bound.
	 */
	public static WildcardType upperBounded(Type type) {
		Supplier<Type[]> types;

		if (Object.class.equals(type))
			return unbounded();
		else if (type instanceof WildcardType) {
			WildcardType wildcardType = ((WildcardType) type);
			if (wildcardType.getLowerBounds().length == 0)
				throw new TypeException(
						"Cannot have define an upper bounding on a wildcard with no lower bounds.");
			else
				types = () -> wildcardType.getLowerBounds();
		} else if (type instanceof IntersectionType)
			types = ((IntersectionType) type)::getTypes;
		else
			types = () -> new Type[] { type };

		return new WildcardType() {
			@Override
			public Type[] getUpperBounds() {
				return types.get();
			}

			@Override
			public Type[] getLowerBounds() {
				return new Type[0];
			}

			@Override
			public String toString() {
				return "? extends "
						+ Arrays.stream(types.get()).map(Types::toString)
								.collect(Collectors.joining(" & "));
			}

			@Override
			public boolean equals(Object that) {
				if (!(that instanceof WildcardType))
					return false;
				if (that == this)
					return true;
				WildcardType wildcard = (WildcardType) that;
				return wildcard.getLowerBounds().length == 0
						&& (Arrays.equals(types.get(), wildcard.getUpperBounds()) || ((getUpperBounds().length == 0 || Arrays
								.equals(getUpperBounds(), new Type[] { Object.class })) && (wildcard
								.getUpperBounds().length == 0 || Arrays.equals(
								wildcard.getUpperBounds(), new Type[] { Object.class }))));
			}

			@Override
			public int hashCode() {
				return Arrays.hashCode(getLowerBounds())
						^ Arrays.hashCode(getUpperBounds());
			}
		};
	}

	static WildcardType fullyBounded(Type upperBound, Type lowerBound) {
		Supplier<Type[]> upperBounds;

		if (Object.class.equals(upperBound))
			return lowerBounded(lowerBound);
		else if (upperBound instanceof WildcardType) {
			WildcardType wildcardType = ((WildcardType) upperBound);
			if (wildcardType.getLowerBounds().length == 0)
				throw new TypeException(
						"Cannot have define an upper bounding on a wildcard with no lower bounds.");
			else
				upperBounds = () -> wildcardType.getLowerBounds();
		} else if (upperBound instanceof IntersectionType)
			upperBounds = ((IntersectionType) upperBound)::getTypes;
		else
			upperBounds = () -> new Type[] { upperBound };

		if (lowerBound instanceof WildcardType) {
			WildcardType wildcardType = ((WildcardType) lowerBound);
			if (wildcardType.getLowerBounds().length > 0
					|| wildcardType.getUpperBounds().length == 0)
				return wildcardType;
		}

		Supplier<Type[]> lowerBounds;

		if (lowerBound instanceof WildcardType) {
			WildcardType wildcardType = ((WildcardType) lowerBound);
			if (wildcardType.getUpperBounds().length == 0)
				lowerBounds = () -> new Type[] { Object.class };
			else
				lowerBounds = () -> wildcardType.getUpperBounds();
		} else if (lowerBound instanceof IntersectionType)
			lowerBounds = ((IntersectionType) lowerBound)::getTypes;
		else
			lowerBounds = () -> new Type[] { lowerBound };

		return new WildcardType() {
			@Override
			public Type[] getUpperBounds() {
				return upperBounds.get();
			}

			@Override
			public Type[] getLowerBounds() {
				return lowerBounds.get();
			}

			@Override
			public String toString() {
				StringBuilder builder = new StringBuilder("?");

				if (upperBounds.get().length > 0)
					builder.append(" extends ").append(
							Arrays.stream(upperBounds.get()).map(Types::toString)
									.collect(Collectors.joining(" & ")));

				if (lowerBounds.get().length > 0)
					builder.append(" super ").append(
							Arrays.stream(lowerBounds.get()).map(Types::toString)
									.collect(Collectors.joining(" & ")));

				return builder.toString();
			}

			@Override
			public boolean equals(Object that) {
				if (!(that instanceof WildcardType))
					return false;
				if (that == this)
					return true;
				WildcardType wildcard = (WildcardType) that;
				return Arrays.equals(upperBounds.get(), wildcard.getUpperBounds())
						&& Arrays.equals(lowerBounds.get(), wildcard.getLowerBounds());
			}

			@Override
			public int hashCode() {
				return Arrays.hashCode(getLowerBounds())
						^ Arrays.hashCode(getUpperBounds());
			}
		};
	}
}

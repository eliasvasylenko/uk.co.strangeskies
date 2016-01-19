/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A collection of utility methods relating to wildcard types.
 * 
 * @author Elias N Vasylenko
 */
public class WildcardTypes {
	private static WildcardType UNBOUNDED = new WildcardType() {
		@Override
		public Type[] getUpperBounds() {
			return new Type[] { Object.class };
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
					&& (wildcard.getUpperBounds().length == 0 || (wildcard
							.getUpperBounds().length == 1 && wildcard.getUpperBounds()[0]
							.equals(Object.class)));
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(getLowerBounds())
					^ Arrays.hashCode(getUpperBounds());
		}
	};

	private WildcardTypes() {}

	/**
	 * Create an unbounded wildcard type.
	 * 
	 * @return An instance of {@link WildcardType} representing an unbounded
	 *         wildcard.
	 */
	public static WildcardType unbounded() {
		return UNBOUNDED;
	}

	/**
	 * Create an lower bounded wildcard type.
	 * 
	 * @param bounds
	 *          The types we wish form the lower bounds for a wildcard.
	 * @return An instance of {@link WildcardType} representing a wildcard with
	 *         the given lower bound.
	 */
	public static WildcardType lowerBounded(Type... bounds) {
		return lowerBounded(Arrays.asList(bounds));
	}

	/**
	 * Create an lower bounded wildcard type.
	 * 
	 * @param bounds
	 *          The types we wish form the lower bounds for a wildcard.
	 * @return An instance of {@link WildcardType} representing a wildcard with
	 *         the given lower bound.
	 */
	public static WildcardType lowerBounded(Collection<? extends Type> bounds) {
		Type type = IntersectionType.from(bounds);

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

				Type[] thatUpperBounds = wildcard.getUpperBounds();
				if (thatUpperBounds.length == 0)
					thatUpperBounds = new Type[] { Object.class };

				return Arrays.equals(types.get(), wildcard.getLowerBounds())
						&& Arrays.equals(thatUpperBounds, new Type[] { Object.class });
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
	 * @param bounds
	 *          The types we wish form the upper bounds for a wildcard.
	 * @return An instance of {@link WildcardType} representing a wildcard with
	 *         the given upper bound.
	 */
	public static WildcardType upperBounded(Type... bounds) {
		return upperBounded(Arrays.asList(bounds));
	}

	/**
	 * Create an upper bounded wildcard type.
	 * 
	 * @param bounds
	 *          The types we wish form the upper bounds for a wildcard.
	 * @return An instance of {@link WildcardType} representing a wildcard with
	 *         the given upper bound.
	 */
	public static WildcardType upperBounded(Collection<? extends Type> bounds) {
		return new WildcardType() {
			private Type[] types;
			private final Supplier<Type[]> typeSupplier = () -> {
				if (bounds.isEmpty()) {
					return new Type[] { Object.class };
				} else if (bounds.size() == 1) {
					return new Type[] { bounds.iterator().next() };
				} else {
					Type type = IntersectionType.from(bounds);

					if (type instanceof WildcardType) {
						WildcardType wildcardType = ((WildcardType) type);
						if (wildcardType.getLowerBounds().length == 0) {
							throw new TypeException(
									"Cannot have define an upper bounding on a wildcard with no lower bounds.");
						} else {
							return wildcardType.getLowerBounds();
						}
					} else if (type instanceof IntersectionType) {
						return ((IntersectionType) type).getTypes();
					} else {
						return new Type[] { type };
					}
				}
			};

			@Override
			public Type[] getUpperBounds() {
				if (types == null)
					types = typeSupplier.get();
				return types;
			}

			@Override
			public Type[] getLowerBounds() {
				return new Type[0];
			}

			@Override
			public String toString() {
				Type[] bounds = getUpperBounds();
				if (bounds.length == 0
						|| (bounds.length == 1 && bounds[0].equals(Object.class)))
					return "?";
				else
					return "? extends "
							+ Arrays.stream(bounds).map(Types::toString)
									.collect(Collectors.joining(" & "));
			}

			@Override
			public boolean equals(Object that) {
				if (!(that instanceof WildcardType))
					return false;
				if (that == this)
					return true;
				WildcardType wildcard = (WildcardType) that;

				Type[] thisUpperBounds = getUpperBounds();
				if (thisUpperBounds.length == 1
						&& thisUpperBounds[0].equals(Object.class))
					thisUpperBounds = new Type[0];

				Type[] thatUpperBounds = wildcard.getUpperBounds();
				if (thatUpperBounds.length == 1
						&& thatUpperBounds[0].equals(Object.class))
					thatUpperBounds = new Type[0];

				return wildcard.getLowerBounds().length == 0
						&& Arrays.equals(thisUpperBounds, thatUpperBounds);
			}

			@Override
			public int hashCode() {
				return Arrays.hashCode(getLowerBounds())
						^ Arrays.hashCode(getUpperBounds());
			}
		};
	}
}

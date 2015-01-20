package uk.co.strangeskies.reflection;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class WildcardTypes {
	private WildcardTypes() {}

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
						&& wildcard.getUpperBounds().length == 0;
			}

			@Override
			public int hashCode() {
				return Arrays.hashCode(getLowerBounds())
						^ Arrays.hashCode(getUpperBounds());
			}
		};
	}

	public static WildcardType lowerBounded(Type type) {
		if (type instanceof WildcardType) {
			WildcardType wildcardType = ((WildcardType) type);
			if (wildcardType.getLowerBounds().length > 0
					|| wildcardType.getUpperBounds().length == 0)
				return wildcardType;
		}
		Supplier<Type[]> types;
		if (type instanceof IntersectionType)
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
						&& wildcard.getUpperBounds().length == 0;
			}

			@Override
			public int hashCode() {
				return Arrays.hashCode(getLowerBounds())
						^ Arrays.hashCode(getUpperBounds());
			}
		};
	}

	public static WildcardType upperBounded(Type type) {
		if (type instanceof WildcardType) {
			WildcardType wildcardType = ((WildcardType) type);
			if (wildcardType.getUpperBounds().length > 0
					|| wildcardType.getLowerBounds().length == 0)
				return wildcardType;
			else
				return unbounded();
		}

		if (Object.class.equals(type))
			return unbounded();

		Supplier<Type[]> types;
		if (type instanceof IntersectionType)
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
				return Arrays.equals(types.get(), wildcard.getUpperBounds())
						&& wildcard.getLowerBounds().length == 0;
			}

			@Override
			public int hashCode() {
				return Arrays.hashCode(getLowerBounds())
						^ Arrays.hashCode(getUpperBounds());
			}
		};
	}
}

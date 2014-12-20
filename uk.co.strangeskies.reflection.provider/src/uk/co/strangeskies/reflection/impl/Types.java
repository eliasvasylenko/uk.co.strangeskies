package uk.co.strangeskies.reflection.impl;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.IntersectionType;

import com.google.common.reflect.TypeToken;

public class Types {
	private Types() {}

	public static boolean isProperType(Type type) {
		return InferenceVariable.getAllMentionedBy(type).isEmpty();
	}

	public static boolean isAssignable(Type from, Type to) {
		if (from == null || from.equals(to)) {
			return true;
		} else if (from instanceof IntersectionType) {
			/*
			 * We must be able to assign from at least one member of the intersection
			 * type.
			 */
			return Arrays.stream(((IntersectionType) from).getTypes()).anyMatch(
					f -> isAssignable(f, to));
		} else if (to instanceof IntersectionType) {
			/*
			 * We must be able to assign to each member of the intersection type.
			 */
			return Arrays.stream(((IntersectionType) to).getTypes()).allMatch(
					t -> isAssignable(from, t));
		} else if (to instanceof WildcardType) {
			/*
			 * This Should be taken care of bye the TypeToken check below, but
			 * currently there is a bug, so we provide a correct implementation here
			 * for the moment.
			 */
			Type[] lowerBounds = ((WildcardType) to).getLowerBounds();
			if (lowerBounds.length == 0)
				return false;
			else
				return isAssignable(from, IntersectionType.of(lowerBounds))
						&& isAssignable(from,
								IntersectionType.of(((WildcardType) to).getUpperBounds()));
		} else if (to instanceof ParameterizedType) {
			TypeToken<?> fromTypeToken = TypeToken.of(from);
			Class<?> matchedClass = TypeToken.of(to).getRawType();

			if (from instanceof Class
					&& matchedClass.isAssignableFrom((Class<?>) from)) {
				return true;
			} else if (!matchedClass.isAssignableFrom(fromTypeToken.getRawType())) {
				return false;
			}
			Type[] typeParams = matchedClass.getTypeParameters();
			Type[] toTypeArgs = ((ParameterizedType) to).getActualTypeArguments();
			for (int i = 0; i < typeParams.length; i++) {
				Type fromTypeArg = fromTypeToken.resolveType(typeParams[i]).getType();
				if (!isContainedBy(fromTypeArg, toTypeArgs[i]))
					return false;
			}

			return isAssignable(((ParameterizedType) from).getOwnerType(),
					((ParameterizedType) to).getOwnerType());
		} else
			return TypeToken.of(to).isAssignableFrom(TypeToken.of(from));
	}

	private static boolean isContainedBy(Type from, Type to) {
		if (to.equals(from))
			return true;

		if (to instanceof Class || to instanceof ParameterizedType)
			return isAssignable(from, to) && isAssignable(to, from);
		else if (to instanceof WildcardType) {
			WildcardType toWildcard = (WildcardType) to;
			return isAssignable(from,
					IntersectionType.of(toWildcard.getUpperBounds()))
					&& (toWildcard.getLowerBounds().length == 0 || isAssignable(
							IntersectionType.of(toWildcard.getLowerBounds()), from));
		} else
			return false;
	}

	public static boolean isStrictInvocationCompatible(Type from, Type to) {
		TypeToken<?> toToken = TypeToken.of(to);
		TypeToken<?> fromToken = TypeToken.of(from);

		if (fromToken.isPrimitive())
			if (toToken.isPrimitive())
				return true; // TODO check widening primitive conversion
			else
				return false;
		else if (toToken.isPrimitive())
			return false;
		else
			return isAssignable(from, to);
	}

	public static boolean isLooselyInvocationCompatible(Type from, Type to) {
		TypeToken<?> toToken = TypeToken.of(to);
		TypeToken<?> fromToken = TypeToken.of(from);

		if (fromToken.isPrimitive() && !toToken.isPrimitive())
			fromToken = fromToken.wrap();
		else if (!fromToken.isPrimitive() && toToken.isPrimitive())
			fromToken = fromToken.unwrap();

		return isStrictInvocationCompatible(from, to);
	}

	public static WildcardType unboundedWildcard() {
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

	public static WildcardType lowerBoundedWildcard(Type type) {
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
						+ Arrays.stream(types.get()).map(Object::toString)
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

	public static WildcardType upperBoundedWildcard(Type type) {
		if (type instanceof WildcardType) {
			WildcardType wildcardType = ((WildcardType) type);
			if (wildcardType.getUpperBounds().length > 0
					|| wildcardType.getLowerBounds().length == 0)
				return wildcardType;
			else
				return unboundedWildcard();
		}

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
						+ Arrays.stream(types.get()).map(Object::toString)
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

	public static ParameterizedType parameterizedType(Type ownerType,
			Class<?> rawType, Type... typeArguments) {
		return new ParameterizedTypeImpl(ownerType, rawType, typeArguments);
	}

	public static ParameterizedType parameterizedType(Class<?> rawClass,
			Map<TypeVariable<?>, Type> typeArguments) {
		return new ParameterizedTypeImpl(
				rawClass.getEnclosingClass() != null ? parameterizedType(
						rawClass.getEnclosingClass(), typeArguments) : null, rawClass,
				argumentsForClass(rawClass, typeArguments));
	}

	private static Type[] argumentsForClass(Class<?> rawClass,
			Map<TypeVariable<?>, Type> typeArguments) {
		Type[] arguments = new Type[rawClass.getTypeParameters().length];
		for (int i = 0; i < arguments.length; i++) {
			arguments[i] = typeArguments.get(rawClass.getTypeParameters()[i]);
			if (arguments[i] == null)
				arguments[i] = rawClass.getTypeParameters()[i];
		}
		return arguments;
	}

	private static final class ParameterizedTypeImpl implements
			ParameterizedType, Serializable {

		private final Type ownerType;
		private final List<Type> argumentsList;
		private final Class<?> rawType;

		ParameterizedTypeImpl(Type ownerType, Class<?> rawType, Type[] typeArguments) {
			// TODO checkNotNull(rawType);
			// checkArgument(typeArguments.length ==
			// rawType.getTypeParameters().length);
			// disallowPrimitiveType(typeArguments, "type parameter");
			this.ownerType = ownerType;
			this.rawType = rawType;
			this.argumentsList = Arrays.asList(typeArguments);
		}

		@Override
		public Type[] getActualTypeArguments() {
			return argumentsList.toArray(new Type[argumentsList.size()]);
		}

		@Override
		public Type getRawType() {
			return rawType;
		}

		@Override
		public Type getOwnerType() {
			return ownerType;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			if (ownerType != null)
				builder.append(ownerType).append('.');
			builder
					.append(rawType.getName())
					.append('<')
					.append(
							argumentsList.stream().map(Type::toString)
									.collect(Collectors.joining(" & "))).append('>');
			return builder.toString();
		}

		@Override
		public int hashCode() {
			return (ownerType == null ? 0 : ownerType.hashCode())
					^ argumentsList.hashCode() ^ rawType.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof ParameterizedType)) {
				return false;
			}
			ParameterizedType that = (ParameterizedType) other;
			return getRawType().equals(that.getRawType())
					&& Objects.equals(getOwnerType(), that.getOwnerType())
					&& Arrays.equals(getActualTypeArguments(),
							that.getActualTypeArguments());
		}

		private static final long serialVersionUID = 0;
	}
}

package uk.co.strangeskies.reflection;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.reflect.TypeToken;

public final class Types {
	public static final Map<Class<?>, Class<?>> WRAPPED_PRIMITIVES = Collections
			.unmodifiableMap(new HashMap<Class<?>, Class<?>>() {
				private static final long serialVersionUID = 1L;
				{
					put(void.class, Void.class);
					put(boolean.class, Boolean.class);
					put(byte.class, Byte.class);
					put(char.class, Character.class);
					put(short.class, Short.class);
					put(int.class, Integer.class);
					put(long.class, Long.class);
					put(float.class, Float.class);
					put(double.class, Double.class);
				}
			});

	public static final Map<Class<?>, Class<?>> UNWRAPPED_PRIMITIVES = Collections
			.unmodifiableMap(new HashMap<Class<?>, Class<?>>() {
				private static final long serialVersionUID = 1L;
				{
					for (Class<?> primitive : WRAPPED_PRIMITIVES.keySet())
						put(WRAPPED_PRIMITIVES.get(primitive), primitive);
				}
			});

	private Types() {}

	public static Class<?> getRawType(Type type) {
		if (type == null) {
			return null;
		} else if (type instanceof TypeVariable) {
			return getRawType(((TypeVariable<?>) type).getBounds()[0]);
		} else if (type instanceof WildcardType) {
			return getRawType(((WildcardType) type).getUpperBounds()[0]);
		} else if (type instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) type).getRawType();
		} else if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof GenericArrayType) {
			return Array.newInstance(
					(getRawType(((GenericArrayType) type).getGenericComponentType())), 0)
					.getClass();
		} else if (type instanceof IntersectionType) {
			return Array.newInstance(
					(getRawType(((IntersectionType) type).getTypes()[0])), 0).getClass();
		}
		throw new IllegalArgumentException("Type of type '" + type
				+ "' is unsupported.");
	}

	public static boolean isPrimitive(Type type) {
		return WRAPPED_PRIMITIVES.keySet().contains(getRawType(type));
	}

	public static boolean isPrimitiveWrapper(Type type) {
		return UNWRAPPED_PRIMITIVES.keySet().contains(getRawType(type));
	}

	public static Type wrap(Type type) {
		if (isPrimitive(type))
			return WRAPPED_PRIMITIVES.get(getRawType(type));
		else
			return type;
	}

	public static Type unwrap(Type type) {
		if (isPrimitiveWrapper(type))
			return UNWRAPPED_PRIMITIVES.get(getRawType(type));
		else
			return type;
	}

	public static boolean isProperType(Type type) {
		return !(type instanceof InferenceVariable)
				&& InferenceVariable.getAllMentionedBy(type).isEmpty();
	}

	public static boolean isAssignable(Type from, Type to) {
		if (from == null || from.equals(to) || to == null
				|| to.equals(Object.class)) {
			/*
			 * We can always assign to or from 'null', and we can always assign to
			 * Object.
			 */
			return true;
		} else if (to instanceof TypeVariable) {
			/*
			 * We can only assign to a type variable if it is from the exact same
			 * type.
			 */
			return false;
		} else if (from instanceof TypeVariable) {
			/*
			 * We must be able to assign from at least one of the upper bound,
			 * including the implied upper bound of Object, to the target type.
			 */
			Type[] upperBounds = ((TypeVariable<?>) from).getBounds();
			if (upperBounds.length == 0)
				upperBounds = new Type[] { Object.class };
			return isAssignable(IntersectionType.of(upperBounds), to);
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
		} else if (from instanceof WildcardType) {
			/*
			 * We must be able to assign from at least one of the upper bound,
			 * including the implied upper bound of Object, to the target type.
			 */
			Type[] upperBounds = ((WildcardType) from).getUpperBounds();
			if (upperBounds.length == 0)
				upperBounds = new Type[] { Object.class };
			return isAssignable(IntersectionType.of(upperBounds), to);
		} else if (to instanceof WildcardType) {
			/*
			 * If there are no lower bounds the target may be arbitrarily specific, so
			 * we can never assign to it. Otherwise we must be able to assign to each
			 * lower bound.
			 */
			Type[] lowerBounds = ((WildcardType) to).getLowerBounds();
			if (lowerBounds.length == 0)
				return false;
			else
				return isAssignable(from, IntersectionType.of(lowerBounds));
		} else if (from instanceof GenericArrayType) {
			GenericArrayType fromArray = (GenericArrayType) from;
			if (to instanceof Class) {
				Class<?> toClass = (Class<?>) to;
				return toClass.isArray()
						&& isAssignable(fromArray.getGenericComponentType(),
								toClass.getComponentType());
			} else if (to instanceof GenericArrayType) {
				GenericArrayType toArray = (GenericArrayType) to;
				return isAssignable(fromArray.getGenericComponentType(),
						toArray.getGenericComponentType());
			} else
				return false;
		} else if (to instanceof GenericArrayType) {
			GenericArrayType toArray = (GenericArrayType) to;
			if (from instanceof Class) {
				Class<?> fromClass = (Class<?>) from;
				return fromClass.isArray()
						&& isAssignable(fromClass.getComponentType(),
								toArray.getGenericComponentType());
			} else
				return false;
		} else if (to instanceof Class) {
			return ((Class<?>) to).isAssignableFrom(getRawType(from));
		} else if (to instanceof ParameterizedType) {
			TypeLiteral<?> fromTypeToken = TypeLiteral.of(from);
			Class<?> matchedClass = TypeLiteral.of(to).getRawType();

			if (from instanceof Class
					&& matchedClass.isAssignableFrom((Class<?>) from)) {
				return true;
			} else if (!matchedClass.isAssignableFrom(fromTypeToken.getRawType())) {
				return false;
			}
			Type[] typeParams = matchedClass.getTypeParameters();
			Type[] toTypeArgs = ((ParameterizedType) to).getActualTypeArguments();
			for (int i = 0; i < typeParams.length; i++) {
				Type fromTypeArg = TypeToken.of(fromTypeToken.getType())
						.resolveType(typeParams[i]).getType();
				if (!isContainedBy(fromTypeArg, toTypeArgs[i]))
					return false;
			}

			return isAssignable(((ParameterizedType) from).getOwnerType(),
					((ParameterizedType) to).getOwnerType());
		} else
			return false;
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

	public static boolean isStrictInvocationContextCompatible(Type from, Type to) {
		if (TypeLiteral.of(from).isPrimitive())
			if (TypeLiteral.of(to).isPrimitive())
				return true; // TODO check widening primitive conversion
			else
				return false;
		else if (TypeLiteral.of(to).isPrimitive())
			return false;
		else
			return isAssignable(from, to);
	}

	public static boolean isLooseInvocationContextCompatible(Type from, Type to) {
		if (isPrimitive(from) && !isPrimitive(to))
			from = wrap(from);
		else if (!isPrimitive(from) && isPrimitive(to))
			from = unwrap(from);
		return isStrictInvocationContextCompatible(from, to);
	}

	public static Set<Type> getAllMentionedBy(Type type, Predicate<Type> condition) {
		Set<Type> types = new HashSet<>();

		Consumer<Type> conditionalAdd = t -> {
			if (condition.test(t))
				types.add(t);
		};

		RecursiveTypeVisitor.build().visitEnclosingTypes().visitParameters()
				.visitBounds().classVisitor(conditionalAdd::accept)
				.genericArrayVisitor(conditionalAdd::accept)
				.intersectionTypeVisitor(conditionalAdd::accept)
				.parameterizedTypeVisitor(conditionalAdd::accept)
				.wildcardVisitor(conditionalAdd::accept)
				.typeVariableVisitor(conditionalAdd::accept).create().visit(type);

		return types;
	}

	public static GenericArrayType genericArrayType(Type type) {
		return () -> type;
	}

	public static GenericArrayType genericArrayType(TypeVariable<?> type) {
		return () -> type;
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

	public static ParameterizedType parameterizedType(Class<?> rawType) {
		return parameterizedType(rawType, new HashMap<>());
	}

	public static ParameterizedType parameterizedType(Class<?> rawType,
			Map<TypeVariable<?>, Type> typeArguments) {
		return new ParameterizedTypeImpl(
				rawType.getEnclosingClass() != null ? parameterizedType(
						rawType.getEnclosingClass(), typeArguments) : null, rawType,
				argumentsForClass(rawType, typeArguments));
	}

	private static Type[] argumentsForClass(Class<?> rawType,
			Map<TypeVariable<?>, Type> typeArguments) {
		Type[] arguments = new Type[rawType.getTypeParameters().length];
		for (int i = 0; i < arguments.length; i++) {
			arguments[i] = typeArguments.get(rawType.getTypeParameters()[i]);
			if (arguments[i] == null)
				arguments[i] = rawType.getTypeParameters()[i];
		}
		return arguments;
	}

	private static final class ParameterizedTypeImpl implements
			ParameterizedType, Serializable {
		private static final long serialVersionUID = 1L;

		private final Type ownerType;
		private final List<Type> typeArguments;
		private final Class<?> rawType;

		private final Set<Thread> recurringThreads;

		ParameterizedTypeImpl(Type ownerType, Class<?> rawType, Type[] typeArguments) {
			this.ownerType = ownerType;
			this.rawType = rawType;
			this.typeArguments = Arrays.asList(typeArguments);

			recurringThreads = new HashSet<>();
		}

		@Override
		public Type[] getActualTypeArguments() {
			return typeArguments.toArray(new Type[typeArguments.size()]);
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
			builder.append(rawType.getName()).append('<');

			if (recurringThreads.add(Thread.currentThread())) {
				builder.append(typeArguments.stream().map(Type::toString)
						.collect(Collectors.joining(" & ")));

				recurringThreads.remove(Thread.currentThread());
			} else
				builder.append("...");

			builder.append('>');

			return builder.toString();
		}

		@Override
		public int hashCode() {
			if (recurringThreads.add(Thread.currentThread())) {
				int hashCode = (ownerType == null ? 0 : ownerType.hashCode())
						^ typeArguments.hashCode() ^ rawType.hashCode();

				recurringThreads.remove(Thread.currentThread());

				return hashCode;
			} else
				return 0;
		}

		@Override
		public boolean equals(Object other) {
			if (recurringThreads.add(Thread.currentThread())) {
				if (!(other instanceof ParameterizedType))
					return false;

				ParameterizedType that = (ParameterizedType) other;

				boolean equals = getRawType().equals(that.getRawType())
						&& Objects.equals(getOwnerType(), that.getOwnerType())
						&& Arrays.equals(getActualTypeArguments(),
								that.getActualTypeArguments());

				recurringThreads.remove(Thread.currentThread());
				return equals;
			} else
				return true;
		}
	}
}

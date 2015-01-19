package uk.co.strangeskies.reflection;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
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
import java.util.stream.Stream;

import org.checkerframework.checker.javari.qual.ReadOnly;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import uk.co.strangeskies.utilities.Property;

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
			Type[] bounds = ((TypeVariable<?>) type).getBounds();
			if (bounds.length == 0)
				return Object.class;
			else
				return getRawType(bounds[0]);
		} else if (type instanceof InferenceVariable) {
			Type[] bounds = ((InferenceVariable) type).getUpperBounds();
			if (bounds.length == 0)
				return Object.class;
			else
				return getRawType(bounds[0]);
		} else if (type instanceof WildcardType) {
			Type[] bounds = ((WildcardType) type).getUpperBounds();
			if (bounds.length == 0)
				return Object.class;
			else
				return getRawType(bounds[0]);
		} else if (type instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) type).getRawType();
		} else if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof GenericArrayType) {
			return Array.newInstance(
					(getRawType(((GenericArrayType) type).getGenericComponentType())), 0)
					.getClass();
		} else if (type instanceof IntersectionType) {
			return getRawType(((IntersectionType) type).getTypes()[0]);
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

	public static Type getComponentType(Type type) {
		if (type instanceof Class)
			return ((Class<?>) type).getComponentType();
		else if (type instanceof GenericArrayType)
			return ((GenericArrayType) type).getGenericComponentType();
		else
			return null;
	}

	public static String toString(Type type) {
		if (type instanceof Class && getRawType(type).isArray())
			return toString(((Class<?>) type).getComponentType()) + "[]";
		else
			return type == null ? "null" : type.getTypeName();
	}

	public static boolean isProperType(Type type) {
		return !(type instanceof InferenceVariable)
				&& InferenceVariable.getAllMentionedBy(type).isEmpty();
	}

	public static boolean isGeneric(Class<?> type) {
		return !getAllTypeParameters(type).isEmpty();
	}

	/**
	 * This method retrieves a list of all type variables present on the given raw
	 * type, as well as all type variables on any enclosing types recursively, in
	 * the order encountered.
	 *
	 * @param rawType
	 * @return
	 */
	public static List<TypeVariable<?>> getAllTypeParameters(Class<?> rawType) {
		Stream<TypeVariable<?>> typeParameters = Stream.empty();
		do {
			typeParameters = Stream.concat(typeParameters,
					Arrays.stream(rawType.getTypeParameters()));
		} while ((rawType = rawType.getEnclosingClass()) != null);
		return typeParameters.collect(Collectors.toList());
	}

	/**
	 * For a given parameterized type, we retrieve a mapping of all type variables
	 * on its raw type, as given by {@link Types#getAllTypeParameters(Class)}, to
	 * their arguments within the context of this type.
	 *
	 * @param type
	 * @return
	 */
	public static Map<TypeVariable<?>, Type> getAllTypeArguments(
			ParameterizedType type) {
		Map<TypeVariable<?>, Type> typeArguments = new HashMap<>();

		Class<?> rawType = getRawType(type);
		do {
			for (int i = 0; i < rawType.getTypeParameters().length; i++) {
				TypeVariable<?> typeParameter = rawType.getTypeParameters()[i];
				Type typeArgument = type.getActualTypeArguments()[i];

				typeArguments.put(typeParameter, typeArgument);
			}

			type = type.getOwnerType() instanceof ParameterizedType ? (ParameterizedType) type
					.getOwnerType() : null;
			rawType = rawType.getEnclosingClass();
		} while (type != null && rawType != null);

		return typeArguments;
	}

	public static boolean isAssignable(Type from, Type to) {
		if (from == null || from.equals(to) || to == null
				|| to.equals(Object.class)) {
			/*
			 * We can always assign to or from 'null', and we can always assign to
			 * Object.
			 */
			return true;
		} else if (to instanceof CaptureType) {
			return ((CaptureType) to).getLowerBounds().length > 0
					&& isAssignable(from,
							IntersectionType.uncheckedOf(((CaptureType) to).getLowerBounds()));
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
			Type[] types = ((IntersectionType) from).getTypes();
			return types.length == 0
					|| Arrays.stream(types).anyMatch(f -> isAssignable(f, to));
		} else if (to instanceof IntersectionType) {
			/*
			 * We must be able to assign to each member of the intersection type.
			 */
			Type[] types = ((IntersectionType) to).getTypes();
			return Arrays.stream(types).allMatch(t -> isAssignable(from, t));
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
			Class<?> matchedClass = getRawType(to);

			if (from instanceof Class
					&& matchedClass.isAssignableFrom((Class<?>) from)) {
				return true;
			} else if (!matchedClass.isAssignableFrom(getRawType(from))) {
				return false;
			}

			Type fromParameterization = TypeLiteral.from(from)
					.resolveSupertypeParameters(matchedClass).getType();
			if (!(fromParameterization instanceof ParameterizedType))
				return false;

			Type[] typeParams = matchedClass.getTypeParameters();
			Type[] toTypeArgs = ((ParameterizedType) to).getActualTypeArguments();
			Type[] fromTypeArgs = ((ParameterizedType) fromParameterization)
					.getActualTypeArguments();

			for (int i = 0; i < typeParams.length; i++) {
				if (!isContainedBy(fromTypeArgs[i], toTypeArgs[i]))
					return false;
			}

			return isAssignable(((ParameterizedType) from).getOwnerType(),
					((ParameterizedType) to).getOwnerType());
		} else
			return false;
	}

	public static boolean isContainedBy(Type from, Type to) {
		if (to.equals(from))
			return true;

		if (to instanceof Class || to instanceof ParameterizedType)
			return isAssignable(from, to) && isAssignable(to, from);
		else if (to instanceof IntersectionType) {
			return Arrays.stream(((IntersectionType) to).getTypes()).allMatch(
					t -> isContainedBy(from, t));
		} else if (to instanceof WildcardType) {
			WildcardType toWildcard = (WildcardType) to;

			boolean contained = (toWildcard.getUpperBounds().length == 0 || isAssignable(
					from, IntersectionType.of(toWildcard.getUpperBounds())));

			contained = contained
					&& (toWildcard.getLowerBounds().length == 0 || isAssignable(
							IntersectionType.of(toWildcard.getLowerBounds()), from));

			return contained;
		} else
			return false;
	}

	public static boolean isStrictInvocationContextCompatible(Type from, Type to) {
		if (isPrimitive(from))
			if (isPrimitive(to))
				return true; // TODO check widening primitive conversion
			else
				return false;
		else if (isPrimitive(to))
			return false;

		return isAssignable(from, to);
	}

	public static boolean isLooseInvocationContextCompatible(Type from, Type to) {
		if (isPrimitive(from) && !isPrimitive(to))
			from = wrap(from);
		else if (!isPrimitive(from) && isPrimitive(to))
			from = unwrap(from);
		return isStrictInvocationContextCompatible(from, to);
	}

	public static void validate(Type type) {
		RecursiveTypeVisitor.build().visitBounds().visitEnclosedTypes()
				.visitEnclosingTypes().visitParameters().visitSupertypes()
				.parameterizedTypeVisitor(TypeLiteral::from)
				.intersectionTypeVisitor(i -> IntersectionType.of(i)).create()
				.visit(type);
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
				.inferenceVariableVisitor(conditionalAdd::accept)
				.parameterizedTypeVisitor(conditionalAdd::accept)
				.wildcardVisitor(conditionalAdd::accept)
				.typeVariableVisitor(conditionalAdd::accept).create().visit(type);

		return types;
	}

	public static GenericArrayType genericArrayType(Type type) {
		return new GenericArrayType() {
			@Override
			public Type getGenericComponentType() {
				return type;
			}

			@Override
			public String toString() {
				return Types.toString(type)
						+ (type instanceof IntersectionType ? " " : "") + "[]";
			}

			@Override
			public boolean equals(@Nullable @ReadOnly Object object) {
				if (this == object)
					return true;
				if (object == null || !(object instanceof GenericArrayType))
					return false;

				GenericArrayType that = (GenericArrayType) object;

				return type.equals(that.getGenericComponentType());
			}

			@Override
			public int hashCode() {
				return type.hashCode();
			}
		};
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

	static ParameterizedType uncheckedParameterizedType(Type ownerType,
			Class<?> rawType, List<Type> typeArguments) {
		return new ParameterizedTypeImpl(ownerType, rawType, typeArguments);
	}

	static <T> Type uncheckedParameterizedType(Class<T> rawType,
			Map<? extends TypeVariable<?>, ? extends Type> typeArguments) {
		Type ownerType = (rawType.getEnclosingClass() == null) ? null
				: uncheckedParameterizedType(rawType.getEnclosingClass(), typeArguments);

		if ((ownerType == null || ownerType instanceof Class)
				&& rawType.getTypeParameters().length == 0)
			return rawType;

		return new ParameterizedTypeImpl(ownerType, rawType, argumentsForClass(
				rawType, typeArguments));
	}

	public static <T> TypeLiteral<? extends T> parameterizedType(Class<T> rawType) {
		return parameterizedType(rawType, new HashMap<>());
	}

	@SuppressWarnings("unchecked")
	public static <T> TypeLiteral<? extends T> parameterizedType(
			Class<T> rawType,
			Map<? extends TypeVariable<?>, ? extends Type> typeArguments) {
		return (TypeLiteral<? extends T>) TypeLiteral
				.from(uncheckedParameterizedType(rawType, typeArguments));
	}

	private static List<Type> argumentsForClass(Class<?> rawType,
			Map<? extends TypeVariable<?>, ? extends Type> typeArguments) {
		List<Type> arguments = new ArrayList<>();
		for (int i = 0; i < rawType.getTypeParameters().length; i++) {
			Type argument = typeArguments.get(rawType.getTypeParameters()[i]);
			arguments.add((argument != null) ? argument
					: rawType.getTypeParameters()[i]);
		}
		return arguments;
	}

	public static ParameterizedType parameterizedTypeProxy(
			Property<?, ParameterizedType> source) {
		return new ParameterizedType() {
			@Override
			public @NonNull Type getRawType() {
				return source.get().getRawType();
			}

			@Override
			public Type getOwnerType() {
				return source.get().getOwnerType();
			}

			@Override
			public @NonNull Type @NonNull [] getActualTypeArguments() {
				return source.get().getActualTypeArguments();
			}

			@Override
			public String toString() {
				return source.get().toString();
			}

			@Override
			public boolean equals(@Nullable @ReadOnly Object arg0) {
				return source.get().equals(arg0);
			}

			@Override
			public int hashCode() {
				return source.get().hashCode();
			}
		};
	}

	private static final class ParameterizedTypeImpl implements
			ParameterizedType, Serializable {
		private static final long serialVersionUID = 1L;

		private final Type ownerType;
		private final List<Type> typeArguments;
		private final Class<?> rawType;

		private final Set<Thread> recurringThreads;
		private final Set<Thread> doublyRecurringThreads;

		ParameterizedTypeImpl(Type ownerType, Class<?> rawType,
				List<Type> typeArguments) {
			this.ownerType = ownerType;
			this.rawType = rawType;
			this.typeArguments = typeArguments;

			recurringThreads = new HashSet<>();
			doublyRecurringThreads = new HashSet<>();
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
			if (ownerType != null) {
				builder.append(ownerType.getTypeName()).append(".");
				builder.append(rawType.getSimpleName());
			} else
				builder.append(rawType.getTypeName());

			builder.append('<');

			Thread currentThread = Thread.currentThread();
			if (recurringThreads.add(currentThread)
					|| doublyRecurringThreads.add(currentThread)) {
				builder.append(typeArguments.stream().map(Types::toString)
						.collect(Collectors.joining(", ")));

				if (!doublyRecurringThreads.remove(currentThread))
					recurringThreads.remove(currentThread);
			} else
				builder.append("...");

			builder.append('>');

			return builder.toString();
		}

		@Override
		public int hashCode() {
			Thread currentThread = Thread.currentThread();
			if (recurringThreads.add(currentThread)) {
				int hashCode = (ownerType == null ? 0 : ownerType.hashCode())
						^ typeArguments.hashCode() ^ rawType.hashCode();

				recurringThreads.remove(currentThread);

				return hashCode;
			} else
				return 0;
		}

		@Override
		public boolean equals(Object other) {
			Thread currentThread = Thread.currentThread();
			if (recurringThreads.add(currentThread)) {
				Boolean equals = null;

				if (other == this)
					equals = true;

				if (equals == null) {
					if (other == null || !(other instanceof ParameterizedType))
						equals = false;

					if (equals == null) {
						ParameterizedType that = (ParameterizedType) other;

						equals = getRawType().equals(that.getRawType())
								&& Objects.equals(getOwnerType(), that.getOwnerType())
								&& Arrays.equals(getActualTypeArguments(),
										that.getActualTypeArguments());
					}
				}

				recurringThreads.remove(currentThread);

				return equals;
			} else
				return true;
		}
	}
}

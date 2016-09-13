package uk.co.strangeskies.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TypeVariables {
	private TypeVariables() {}

	/**
	 * Create an unbounded wildcard type.
	 * 
	 * @param <T>
	 *          the type of the generic declaration
	 * 
	 * @return An instance of {@link WildcardType} representing an unbounded
	 *         wildcard.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends GenericDeclaration> TypeVariable<T> unbounded(T declaration, int i) {
		return new TypeVariable<T>() {
			private final Map<Class<?>, Annotation> annotations = new HashMap<>();

			@Override
			public Type[] getBounds() {
				return new Type[] { Object.class };
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

				return wildcard.getLowerBounds().length == 0 && (wildcard.getUpperBounds().length == 0
						|| (wildcard.getUpperBounds().length == 1 && wildcard.getUpperBounds()[0].equals(Object.class)));
			}

			@Override
			public int hashCode() {
				return Arrays.hashCode(getBounds());
			}

			@Override
			public <U extends Annotation> U getAnnotation(Class<U> annotationClass) {
				return annotations.get(annotationClass);
			}

			@Override
			public Annotation[] getAnnotations() {
				// TODO Auto-generated method stub
				
				/*
				 * 
				 * 
				 * 
				 * 
				 * TODO copy the way this is done from AnnotatedTypeImpl
				 * 
				 * 
				 * 
				 * 
				 * 
				 */
				
				return null;
			}

			@Override
			public Annotation[] getDeclaredAnnotations() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public T getGenericDeclaration() {
				return declaration;
			}

			@Override
			public String getName() {
				return "T" + i;
			}

			@Override
			public AnnotatedType[] getAnnotatedBounds() {
				// TODO Auto-generated method stub
				return null;
			}
		};
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
			private Integer hashCode;

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
				return "? super " + Arrays.stream(types.get()).map(Types::toString).collect(Collectors.joining(" & "));
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
			public synchronized int hashCode() {
				if (hashCode == null) {
					/*
					 * This way the hash code will return 0 if we encounter it again in
					 * the parameters, rather than recurring infinitely:
					 * 
					 * (this is not a problem for other threads as hashCode is
					 * synchronized)
					 */
					hashCode = 0;

					/*
					 * Calculate the hash code properly, now we're guarded against
					 * recursion:
					 */
					this.hashCode = Arrays.hashCode(getLowerBounds()) ^ Arrays.hashCode(getUpperBounds());
				}

				return hashCode;
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
	public static TypeVariable<?> upperBounded(Type... bounds) {
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
			private Integer hashCode;
			private Type[] types;
			private final Runnable typeInitialiser = () -> {
				if (bounds.isEmpty()) {
					types = new Type[] { Object.class };
				} else {
					types = bounds.toArray(new Type[bounds.size()]);
					Type type = IntersectionType.from(bounds);

					if (type instanceof WildcardType) {
						WildcardType wildcardType = ((WildcardType) type);
						if (wildcardType.getLowerBounds().length == 0) {
							throw new ReflectionException(p -> p.invalidUpperBound(wildcardType));
						} else {
							types = wildcardType.getLowerBounds();
						}
					} else if (type instanceof IntersectionType) {
						types = ((IntersectionType) type).getTypes();
					} else {
						types = new Type[] { type };
					}
				}
			};

			@Override
			public Type[] getUpperBounds() {
				if (types == null)
					typeInitialiser.run();
				return types;
			}

			@Override
			public Type[] getLowerBounds() {
				return new Type[0];
			}

			@Override
			public String toString() {
				Type[] bounds = getUpperBounds();
				if (bounds.length == 0 || (bounds.length == 1 && bounds[0].equals(Object.class)))
					return "?";
				else
					return "? extends " + Arrays.stream(bounds).map(Types::toString).collect(Collectors.joining(" & "));
			}

			@Override
			public boolean equals(Object that) {
				if (!(that instanceof WildcardType))
					return false;
				if (that == this)
					return true;
				WildcardType wildcard = (WildcardType) that;

				Type[] thisUpperBounds = getUpperBounds();
				if (thisUpperBounds.length == 1 && thisUpperBounds[0].equals(Object.class))
					thisUpperBounds = new Type[0];

				Type[] thatUpperBounds = wildcard.getUpperBounds();
				if (thatUpperBounds.length == 1 && thatUpperBounds[0].equals(Object.class))
					thatUpperBounds = new Type[0];

				return wildcard.getLowerBounds().length == 0 && Arrays.equals(thisUpperBounds, thatUpperBounds);
			}

			@Override
			public synchronized int hashCode() {
				if (hashCode == null) {
					/*
					 * This way the hash code will return 0 if we encounter it again in
					 * the parameters, rather than recurring infinitely:
					 * 
					 * (this is not a problem for other threads as hashCode is
					 * synchronized)
					 */
					hashCode = 0;

					/*
					 * Calculate the hash code properly, now we're guarded against
					 * recursion:
					 */
					this.hashCode = Arrays.hashCode(getLowerBounds()) ^ Arrays.hashCode(getUpperBounds());
				}

				return hashCode;
			}
		};
	}
}

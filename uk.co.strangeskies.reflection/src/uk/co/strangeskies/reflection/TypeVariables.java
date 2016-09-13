package uk.co.strangeskies.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A collection of utility methods relating to type variables.
 * 
 * @author Elias N Vasylenko
 */
public class TypeVariables {
	private TypeVariables() {}

	/**
	 * Create an unbounded wildcard type.
	 * 
	 * @param declaration
	 *          the containing generic declaration
	 * @param name
	 *          the name of the type variable
	 * @param <T>
	 *          the type of the generic declaration
	 * @return An instance of {@link WildcardType} representing an unbounded
	 *         wildcard.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends GenericDeclaration> TypeVariable<T> unbounded(T declaration, String name) {
		return new TypeVariable<T>() {
			private final Map<Class<? extends Annotation>, Annotation> annotations = new LinkedHashMap<>();

			@Override
			public Type[] getBounds() {
				return new Type[] { Object.class };
			}

			@Override
			public String toString() {
				return getName();
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
			public final <U extends Annotation> U getAnnotation(Class<U> annotationClass) {
				return (U) annotations.get(annotationClass);
			}

			@Override
			public final Annotation[] getAnnotations() {
				return annotations.values().toArray(new Annotation[annotations.size()]);
			}

			@Override
			public final Annotation[] getDeclaredAnnotations() {
				return getAnnotations();
			}

			@Override
			public T getGenericDeclaration() {
				return declaration;
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public AnnotatedType[] getAnnotatedBounds() {
				return new AnnotatedType[0];
			}
		};
	}

	/**
	 * Create an upper bounded wildcard type.
	 * 
	 * @param declaration
	 *          the containing generic declaration
	 * @param name
	 *          the name of the type variable
	 * @param bounds
	 *          The types we wish form the upper bounds for a wildcard.
	 * @param <T>
	 *          the type of the generic declaration
	 * @return An instance of {@link WildcardType} representing a wildcard with
	 *         the given upper bound.
	 */
	public static <T extends GenericDeclaration> TypeVariable<T> upperBounded(T declaration, String name,
			AnnotatedType... bounds) {
		return upperBounded(declaration, name, Arrays.asList(bounds));
	}

	/**
	 * Create an upper bounded wildcard type.
	 * 
	 * @param declaration
	 *          the containing generic declaration
	 * @param name
	 *          the name of the type variable
	 * @param bounds
	 *          The types we wish form the upper bounds for a wildcard.
	 * @param <T>
	 *          the type of the generic declaration
	 * @return An instance of {@link WildcardType} representing a wildcard with
	 *         the given upper bound.
	 */
	public static <T extends GenericDeclaration> TypeVariable<T> upperBounded(T declaration, String name,
			Collection<? extends AnnotatedType> bounds) {
		return upperBounded(declaration, name, Collections.emptySet(), bounds);
	}

	/**
	 * Create an upper bounded wildcard type.
	 * 
	 * @param declaration
	 *          the containing generic declaration
	 * @param name
	 *          the name of the type variable
	 * @param annotations
	 *          the annotations to be declared on the new type variable
	 * @param bounds
	 *          The types we wish form the upper bounds for a wildcard.
	 * @param <T>
	 *          the type of the generic declaration
	 * @return An instance of {@link WildcardType} representing a wildcard with
	 *         the given upper bound.
	 */
	public static <T extends GenericDeclaration> TypeVariable<T> upperBounded(T declaration, String name,
			Collection<Annotation> annotations, AnnotatedType... bounds) {
		return upperBounded(declaration, name, annotations, Arrays.asList(bounds));
	}

	/**
	 * Create an upper bounded wildcard type.
	 * 
	 * @param declaration
	 *          the containing generic declaration
	 * @param name
	 *          the name of the type variable
	 * @param annotations
	 *          the annotations to be declared on the new type variable
	 * @param bounds
	 *          The types we wish form the upper bounds for a wildcard.
	 * @param <T>
	 *          the type of the generic declaration
	 * @return An instance of {@link WildcardType} representing a wildcard with
	 *         the given upper bound.
	 */
	public static <T extends GenericDeclaration> TypeVariable<T> upperBounded(T declaration, String name,
			Collection<Annotation> annotations, Collection<? extends AnnotatedType> bounds) {
		return new TypeVariable<T>() {
			private final Map<Class<? extends Annotation>, Annotation> annotations = new LinkedHashMap<>();

			@Override
			public Type[] getBounds() {
				return new Type[] { Object.class };
			}

			@Override
			public String toString() {
				return getName();
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

			@SuppressWarnings("unchecked")
			@Override
			public final <U extends Annotation> U getAnnotation(Class<U> annotationClass) {
				return (U) annotations.get(annotationClass);
			}

			@Override
			public final Annotation[] getAnnotations() {
				return annotations.values().toArray(new Annotation[annotations.size()]);
			}

			@Override
			public final Annotation[] getDeclaredAnnotations() {
				return getAnnotations();
			}

			@Override
			public T getGenericDeclaration() {
				return declaration;
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public AnnotatedType[] getAnnotatedBounds() {
				return null;
			}
		};
	}
}

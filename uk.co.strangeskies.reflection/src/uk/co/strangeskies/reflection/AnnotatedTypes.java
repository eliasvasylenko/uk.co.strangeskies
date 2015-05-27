package uk.co.strangeskies.reflection;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * A collection of general utility methods relating to annotated types within
 * the Java type system. Utilities related to more specific classes of type may
 * be found in {@link WildcardTypes}, {@link ParameterizedTypes}, and
 * {@link GenericArrayTypes}.
 * 
 * @author Elias N Vasylenko
 */
public final class AnnotatedTypes {
	private AnnotatedTypes() {}

	public static AnnotatedType over(Type type) {
		if (type instanceof Class) {
			return new AnnotatedTypeImpl(type);
		} else if (type instanceof ParameterizedType) {
			return new AnnotatedParameterizedTypeImpl(type);
		}

		throw new IllegalArgumentException("Unexpected class '" + type.getClass()
				+ "' of type '" + type + "'.");
	}
}

package uk.co.strangeskies.reflection;

import java.lang.reflect.Type;

import org.osgi.annotation.versioning.ProviderType;

/**
 * An intersection type, as described in the Java 8 language specification.
 * Roughly, such a type generally behaves as a class which extends each of its
 * component types, but is otherwise an empty definition.
 * 
 * @author Elias N Vasylenko
 */
@ProviderType
public interface IntersectionType extends Type {
	/**
	 * @return Each type which is a member of this intersection.
	 */
	Type[] getTypes();
}

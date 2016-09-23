package uk.co.strangeskies.reflection;

/**
 * This interface is not for outside consumption. It allows instances of
 * {@link ClassDefinition class definitions} which are created reflectively to
 * simulate normal class state by providing access to {@link FieldDefinition
 * defined fields}.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the actual type of the instance
 */
interface ReflectiveInstance<T> {
	ClassDefinition<T> getReflectiveClassDefinition();

	<U> U getReflectiveFieldValue(FieldDefinition<? super T, U> field);

	<U> void setReflectiveFieldValue(FieldDefinition<? super T, U> field, U value);

	T castReflectiveInstance();
}

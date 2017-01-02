package uk.co.strangeskies.reflection.codegen;

public interface ReflectiveInstance<E, T> {
	ClassDefinition<E, T> getClassDefinition();

	<U> U getReflectiveFieldValue(FieldDeclaration<? super T, U> field);

	<U> void setReflectiveFieldValue(FieldDeclaration<? super T, U> field, U value);

	T cast();
}

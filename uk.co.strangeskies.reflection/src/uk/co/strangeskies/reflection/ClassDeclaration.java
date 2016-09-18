package uk.co.strangeskies.reflection;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Separating the logic for declaring the class into a builder allows us to
 * ensure the type of the class is immutable once an actual
 * {@link ClassDefinition} object is instantiated. This means that the type can
 * be safely reasoned about before any class members are defined or any
 * implementation details are specified.
 * 
 * @author Elias N Vasylenko
 * 
 * @param <T>
 *          the intersection of the supertypes of the described class
 */
public class ClassDeclaration<T> extends ParameterizedDeclaration {
	public static ClassDeclaration<Object> declareClass(String typeName) {
		return new ClassDeclaration<>(typeName);
	}

	private final String typeName;
	private final List<AnnotatedType> superType;

	protected ClassDeclaration(String typeName) {
		this.typeName = typeName;
		superType = new ArrayList<>();
	}

	protected String getTypeName() {
		return typeName;
	}

	protected List<AnnotatedType> getSuperTypes() {
		return superType;
	}

	/**
	 * @param superType
	 *          the supertype for the class signature
	 * @return the receiver
	 */
	public ClassDeclaration<?> withSuperType(Type... superType) {
		return withSuperType(Arrays.stream(superType).map(AnnotatedTypes::over).collect(Collectors.toList()));
	}

	/**
	 * @param superType
	 *          the supertype for the class signature
	 * @return the receiver
	 */
	public ClassDeclaration<?> withSuperType(AnnotatedType... superType) {
		return withSuperType(Arrays.asList(superType));
	}

	/**
	 * @param <U>
	 *          the supertype for the class signature
	 * @param superType
	 *          the supertype for the class signature
	 * @return the receiver
	 */
	public <U extends T> ClassDeclaration<U> withSuperType(Class<U> superType) {
		return withSuperType(TypeToken.over(superType));
	}

	/**
	 * @param <U>
	 *          the supertype for the class signature
	 * @param superType
	 *          the supertype for the class signature
	 * @return the receiver
	 */
	@SuppressWarnings("unchecked")
	public <U extends T> ClassDeclaration<U> withSuperType(TypeToken<U> superType) {
		return (ClassDeclaration<U>) withSuperType(superType.getAnnotatedDeclaration());
	}

	/**
	 * @param <U>
	 *          the supertype for the class signature
	 * @param superType
	 *          the supertype for the class signature
	 * @return the receiver
	 */
	@SafeVarargs
	@SuppressWarnings("unchecked")
	public final <U extends T> ClassDeclaration<U> withSuperType(TypeToken<? extends U>... superType) {
		return (ClassDeclaration<U>) withSuperType(
				Arrays.stream(superType).map(TypeToken::getAnnotatedDeclaration).collect(Collectors.toList()));
	}

	/**
	 * @param superType
	 *          the supertype for the class signature
	 * @return the receiver
	 */
	public ClassDeclaration<?> withSuperType(Collection<? extends AnnotatedType> superType) {
		this.superType.clear();
		this.superType.addAll(superType);
		return this;
	}

	public ClassDefinition<? extends T> define() {
		return new ClassDefinition<>(this);
	}
}

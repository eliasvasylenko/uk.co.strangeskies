package uk.co.strangeskies.reflection;

import static uk.co.strangeskies.reflection.WildcardTypes.unbounded;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * A type safe wrapper around {@link Executable} instances, with proper handling
 * of generic methods, and methods on generic classes. Instances of this class
 * can be created from instances of Executable directly from
 * {@link #over(Field)} and its overloads, or using the
 * {@link TypeToken#resolveConstructorOverload(List)} and
 * {@link TypeToken#resolveMethodOverload(String, List)} methods on TypeToken
 * instances.
 * 
 * <p>
 * {@link ExecutableMember invokables} may be created over types which mention
 * inference variables, or even over inference variables themselves.
 * 
 * @author Elias N Vasylenko
 *
 * @param <O>
 *          the owner type which the field belongs to
 * @param <T>
 *          the type of the field
 */
public class FieldMember<O, T> implements TypeMember<O> {
	private final TypeToken<O> ownerType;
	private final TypeToken<T> fieldType;
	private final Field field;

	private FieldMember(Field field, TypeToken<O> ownerType, TypeToken<T> fieldType) {
		this.field = field;
		this.ownerType = ownerType;
		this.fieldType = fieldType;
	}

	public static FieldMember<?, ?> over(Field field) {
		return over(field, ParameterizedTypes.from(field.getDeclaringClass(), a -> unbounded()));
	}

	public static <O> FieldMember<O, ?> over(Field field, TypeToken<O> ownerType) {
		return over(field, ownerType, TypeToken.over(ownerType.resolveType(field.getGenericType())));
	}

	public static <O, T> FieldMember<O, T> over(Field field, TypeToken<O> ownerType, TypeToken<T> fieldType) {
		return new FieldMember<>(field, ownerType, fieldType);
	}

	@Override
	public String getName() {
		return field.getName();
	}

	@Override
	public Field getMember() {
		return field;
	}

	@Override
	public Resolver getResolver() {
		return ownerType.getResolver();
	}

	@Override
	public boolean isFinal() {
		return Modifier.isFinal(field.getModifiers());
	}

	@Override
	public boolean isPrivate() {
		return Modifier.isPrivate(field.getModifiers());
	}

	@Override
	public boolean isProtected() {
		return Modifier.isProtected(field.getModifiers());
	}

	@Override
	public boolean isPublic() {
		return Modifier.isPublic(field.getModifiers());
	}

	@Override
	public boolean isStatic() {
		return Modifier.isStatic(field.getModifiers());
	}

	/**
	 * @return true if the wrapped field is volatile, false otherwise
	 */
	public boolean isVolatile() {
		return Modifier.isVolatile(field.getModifiers());
	}

	/**
	 * @return true if the wrapped field is transient, false otherwise
	 */
	public boolean isTransient() {
		return Modifier.isTransient(field.getModifiers());
	}

	@Override
	public TypeToken<O> getOwnerType() {
		return ownerType;
	}

	/**
	 * @return the exact generic type of the field according to the type of its
	 *         owner
	 */
	public TypeToken<T> getFieldType() {
		return fieldType;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FieldMember<O, T> withBounds(BoundSet bounds) {
		return (FieldMember<O, T>) over(field, ownerType.withBounds(bounds));
	}

	@SuppressWarnings("unchecked")
	@Override
	public FieldMember<O, T> withBounds(BoundSet bounds, Collection<? extends InferenceVariable> inferenceVariables) {
		return (FieldMember<O, T>) over(field, ownerType.withBounds(bounds, inferenceVariables));
	}

	@SuppressWarnings("unchecked")
	@Override
	public FieldMember<O, T> withBoundsFrom(TypeToken<?> type) {
		return (FieldMember<O, T>) over(field, ownerType.withBoundsFrom(type));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U extends O> FieldMember<U, ? extends T> withOwnerType(TypeToken<U> type) {
		return (FieldMember<U, ? extends T>) withBoundsFrom(type).withOwnerType(type.getType());
	}

	@Override
	public FieldMember<? extends O, ? extends T> withOwnerType(Type type) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FieldMember<O, T> infer() {
		return (FieldMember<O, T>) over(field, ownerType.infer());
	}
}

/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
 * {@link InvocableMember executable members} may be created over types which
 * mention inference variables, or even over inference variables themselves.
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

	/**
	 * Create a new {@link FieldMember} instance from a reference to a
	 * {@link Field}.
	 * 
	 * @param field
	 *          the field to wrap
	 * @return a field member wrapping the given field
	 */
	public static FieldMember<?, ?> over(Field field) {
		return over(field, ParameterizedTypes.from(field.getDeclaringClass(), a -> unbounded()));
	}

	/**
	 * Create a new {@link FieldMember} instance from a reference to a
	 * {@link Field}.
	 * 
	 * @param <O>
	 *          the type of the owner
	 * @param field
	 *          the field to wrap
	 * @param ownerType
	 *          the type to which the field belongs
	 * @return a field member wrapping the given field
	 */
	public static <O> FieldMember<O, ?> over(Field field, TypeToken<O> ownerType) {
		return over(field, ownerType, TypeToken.over(ownerType.resolveType(field.getGenericType())));
	}

	/**
	 * Create a new {@link FieldMember} instance from a reference to a
	 * {@link Field}.
	 * 
	 * @param <O>
	 *          the type of the owner
	 * @param <T>
	 *          the type of the field
	 * @param field
	 *          the field to wrap
	 * @param ownerType
	 *          the type to which the field belongs
	 * @param fieldType
	 *          the type of the field
	 * @return a field member wrapping the given field
	 */
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

	public T get(O target) {
		try {
			return (T) field.get(target);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new TypeException(p -> p.cannotGetField(target, this), e);
		}
	}

	public void set(O target, T value) {
		try {
			field.set(target, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new TypeException(p -> p.cannotSetField(target, value, this), e);
		}
	}
}

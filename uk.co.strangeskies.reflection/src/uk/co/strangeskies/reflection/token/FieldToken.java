/*
 * Copyright (C) 2016 ${copyright.holder.name} <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
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
package uk.co.strangeskies.reflection.token;

import static uk.co.strangeskies.reflection.WildcardTypes.unbounded;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.InferenceVariable;
import uk.co.strangeskies.reflection.ParameterizedTypes;
import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.TypeResolver;

/**
 * <p>
 * A type safe wrapper around {@link Executable} instances, with proper handling
 * of generic methods, and methods on generic classes.
 * 
 * <p>
 * {@link ExecutableToken executable members} may be created over types which
 * mention inference variables, or even over inference variables themselves.
 * 
 * @author Elias N Vasylenko
 *
 * @param <O>
 *          the owner type which the field belongs to
 * @param <T>
 *          the type of the field
 */
public class FieldToken<O, T> implements MemberToken<O> {
	private final TypeToken<O> ownerType;
	private final TypeToken<T> fieldType;
	private final Field field;

	private FieldToken(Field field, TypeToken<O> ownerType, TypeToken<T> fieldType) {
		this.field = field;
		this.ownerType = ownerType;
		this.fieldType = fieldType;
	}

	/**
	 * Create a new {@link FieldToken} instance from a reference to a
	 * {@link Field}.
	 * 
	 * @param field
	 *          the field to wrap
	 * @return a field member wrapping the given field
	 */
	public static FieldToken<?, ?> over(Field field) {
		return over(field, TypeToken.overType(ParameterizedTypes.parameterize(field.getDeclaringClass(), a -> unbounded())));
	}

	/**
	 * Create a new {@link FieldToken} instance from a reference to a
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
	public static <O> FieldToken<O, ?> over(Field field, TypeToken<O> ownerType) {
		return new FieldToken<>(field, ownerType, TypeToken.overType(ownerType.resolveType(field.getGenericType())));
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
	public TypeResolver getResolver() {
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
	public TypeToken<O> getReceiverType() {
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
	public FieldToken<O, T> withBounds(BoundSet bounds) {
		return (FieldToken<O, T>) over(field, ownerType.withBounds(bounds));
	}

	@SuppressWarnings("unchecked")
	@Override
	public FieldToken<O, T> withBounds(BoundSet bounds, Collection<? extends InferenceVariable> inferenceVariables) {
		return (FieldToken<O, T>) over(field, ownerType.withBounds(bounds, inferenceVariables));
	}

	@SuppressWarnings("unchecked")
	@Override
	public FieldToken<O, T> withBoundsFrom(TypeToken<?> type) {
		return (FieldToken<O, T>) over(field, ownerType.withBoundsFrom(type));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U extends O> FieldToken<U, ? extends T> withReceiverType(TypeToken<U> type) {
		return (FieldToken<U, ? extends T>) withBoundsFrom(type).withReceiverType(type.getType());
	}

	@Override
	public FieldToken<? extends O, ? extends T> withReceiverType(Type type) {
		return new FieldToken<>(field, ownerType, fieldType);
	}

	@SuppressWarnings("unchecked")
	public <U> FieldToken<O, U> withType(TypeToken<U> type) {
		return (FieldToken<O, U>) withBoundsFrom(type).withType(type.getType());
	}

	public FieldToken<O, ? extends T> withType(Type type) {
		getFieldType().withLooseCompatibilityTo(type);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FieldToken<O, T> infer() {
		return (FieldToken<O, T>) over(field, ownerType.infer());
	}

	/**
	 * @param target
	 *          the instance to access the field of
	 * @return the value of the field
	 */
	@SuppressWarnings("unchecked")
	public T get(O target) {
		try {
			return (T) field.get(target);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new ReflectionException(p -> p.cannotGetField(target, this), e);
		}
	}

	/**
	 * @param target
	 *          the instance to assign to the field of
	 * @param value
	 *          the value to assign
	 */
	public void set(O target, T value) {
		try {
			field.set(target, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new ReflectionException(p -> p.cannotSetField(target, value, this), e);
		}
	}

	/**
	 * Find which fields can be resolved on this type.
	 * 
	 * @param type
	 *          the type for which to retrieve the set of fields
	 * 
	 * @return A list of all {@link Field} objects applicable to this type,
	 *         wrapped in {@link FieldToken} instances.
	 */
	public static <T> Set<? extends FieldToken<T, ?>> getFields(TypeToken<T> type) {
		return getFields(type, c -> true);
	}

	static <T> Set<? extends FieldToken<T, ?>> getFields(TypeToken<T> type, Predicate<Field> filter) {
		return getFieldsImpl(type, filter, Class::getFields);
	}

	/**
	 * Find which fields are declared on this type.
	 * 
	 * @param type
	 *          the type for which to retrieve the set of fields
	 * 
	 * @return A list of all {@link Field} objects applicable to this type,
	 *         wrapped in {@link FieldToken} instances.
	 */
	public static <T> Set<? extends FieldToken<T, ?>> getDeclaredFields(TypeToken<T> type) {
		return getDeclaredFields(type, c -> true);
	}

	static <T> Set<? extends FieldToken<T, ?>> getDeclaredFields(TypeToken<T> type, Predicate<Field> filter) {
		return getFieldsImpl(type, filter, Class::getDeclaredFields);
	}

	private static <T> Set<? extends FieldToken<T, ?>> getFieldsImpl(TypeToken<T> type, Predicate<Field> filter,
			Function<Class<?>, Field[]> fields) {
		return Arrays.stream(fields.apply(type.getRawType())).filter(filter)
				.map(m -> (FieldToken<T, ?>) FieldToken.over(m, type)).collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * Resolve the most specific accessible field on this type and its supertypes
	 * which match the given name.
	 * 
	 * @param name
	 *          the name of the field
	 * @return a field matching the given name
	 */
	public static <T> FieldToken<T, ?> resolveField(TypeToken<T> type, String name) {
		return resolveFieldsImpl(type, name, p -> getFields(type, p)).stream().findFirst().get();
	}

	/**
	 * Resolve the declared field on this type which match the given name.
	 * 
	 * @param name
	 *          the name of the field
	 * @return a field matching the given name
	 */
	public static <T> FieldToken<T, ?> resolveDeclaredField(TypeToken<T> type, String name) {
		return resolveFieldsImpl(type, name, p -> getDeclaredFields(type, p)).stream().findFirst().get();
	}

	/**
	 * Resolve all accessible fields on this type and its supertypes which match
	 * the given name.
	 * 
	 * @param name
	 *          the name of the field
	 * @return a field matching the given name
	 */
	public static <T> List<FieldToken<T, ?>> resolveFields(TypeToken<T> type, String name) {
		return resolveFieldsImpl(type, name, p -> getFields(type, p));
	}

	private static <T> List<FieldToken<T, ?>> resolveFieldsImpl(TypeToken<T> type, String name,
			Function<Predicate<Field>, Set<? extends FieldToken<T, ?>>> fields) {
		Set<? extends FieldToken<T, ? extends Object>> candidates = fields.apply(m -> m.getName().equals(name));

		if (candidates.isEmpty())
			throw new IllegalArgumentException("Cannot find any field '" + name + "' in '" + type + "'");

		return new ArrayList<>(candidates);
	}
}

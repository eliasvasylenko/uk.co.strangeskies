/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;
import static uk.co.strangeskies.reflection.token.FieldTokenQuery.fieldQuery;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;
import static uk.co.strangeskies.reflection.token.TypeToken.forType;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.ConstraintFormula;
import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.TypeResolver;
import uk.co.strangeskies.reflection.TypeSubstitution;
import uk.co.strangeskies.reflection.token.TypeToken.Wildcards;

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
public class FieldToken<O, T> implements MemberToken<O, FieldToken<O, T>> {
	private final TypeToken<? super O> receiverType;
	private final TypeToken<T> fieldType;
	private final Field field;

	protected FieldToken(Field field, TypeToken<? super O> receiverType) {
		this(field, receiverType, new TypeResolver());
	}

	protected FieldToken(Field field, TypeToken<? super O> receiverType, TypeResolver resolver) {
		this.field = field;

		TypeSubstitution inferenceVariableSubstitution = new TypeSubstitution(
				getAllTypeArguments().collect(toMap(TypeArgument::getParameter, TypeArgument::getType)));

		this.receiverType = determineReceiverType(inferenceVariableSubstitution, receiverType);
		this.fieldType = determineFieldType(resolver, inferenceVariableSubstitution);
	}

	private TypeToken<? super O> determineReceiverType(TypeSubstitution inferenceVariables, TypeToken<? super O> receiverType) {
		if (receiverType.getType().equals(void.class)) {
			return receiverType;
		} else {
			return receiverType.resolve();
		}
	}

	@SuppressWarnings("unchecked")
	private TypeToken<T> determineFieldType(TypeResolver resolver, TypeSubstitution inferenceVariables) {
		Type genericReturnType = inferenceVariables.resolve(getMember().getGenericType());

		TypeToken<T> returnType = (TypeToken<T>) forType(resolver.getBounds(), genericReturnType, Wildcards.RETAIN);

		return returnType.resolve();
	}

	/**
	 * Create a new {@link FieldToken} instance from a reference to a
	 * {@link Field}.
	 * 
	 * @param field
	 *          the field to wrap
	 * @return a field member wrapping the given field
	 */
	public static FieldToken<Void, ?> overStaticField(Field field) {
		return new FieldToken<>(field, forClass(void.class), null);
	}

	/**
	 * Create a new {@link FieldToken} instance from a reference to a
	 * {@link Field}.
	 * 
	 * @param field
	 *          the field to wrap
	 * @return a field member wrapping the given field
	 */
	public static <E> FieldToken<E, ?> overStaticField(Field field, TypeToken<E> enclosingInstance) {
		return new FieldToken<>(field, enclosingInstance, null);
	}

	/**
	 * Create a new {@link FieldToken} instance from a reference to a
	 * {@link Field}.
	 * 
	 * @param field
	 *          the field to wrap
	 * @return a field member wrapping the given field
	 */
	public static FieldToken<?, ?> overField(Field field) {
		return new FieldToken<>(field, forClass(field.getDeclaringClass()), null);
	}

	/**
	 * Create a new {@link FieldToken} instance from a reference to a
	 * {@link Field}.
	 * 
	 * @param <O>
	 *          the type of the owner
	 * @param field
	 *          the field to wrap
	 * @param instance
	 *          the type to which the field belongs
	 * @return a field member wrapping the given field
	 */
	public static <O> FieldToken<O, ?> overField(Field field, TypeToken<O> instance) {
		return new FieldToken<>(field, instance, null);
	}

	@Override
	public String getName() {
		return getMember().getName();
	}

	@Override
	public Field getMember() {
		return field;
	}

	@Override
	public BoundSet getBounds() {
		return receiverType.getBounds();
	}

	/**
	 * @return true if the wrapped field is volatile, false otherwise
	 */
	public boolean isVolatile() {
		return Modifier.isVolatile(getMember().getModifiers());
	}

	/**
	 * @return true if the wrapped field is transient, false otherwise
	 */
	public boolean isTransient() {
		return Modifier.isTransient(getMember().getModifiers());
	}

	@Override
	public TypeToken<? super O> getReceiverType() {
		return receiverType;
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
		return (FieldToken<O, T>) overField(getMember(), receiverType.withBounds(bounds));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <P> FieldToken<P, ?> withReceiverType(TypeToken<P> type) {
		return (FieldToken<P, ?>) withBounds(type.getBounds()).withReceiverType(type.getType());
	}

	@Override
	public FieldToken<O, T> withReceiverType(Type type) {
		return new FieldToken<>(getMember(), receiverType);
	}

	@SuppressWarnings("unchecked")
	public <U> FieldToken<O, U> withType(TypeToken<U> type) {
		return (FieldToken<O, U>) withBounds(type.getBounds()).withType(type.getType());
	}

	public FieldToken<O, ? extends T> withType(Type type) {
		return withTypeCapture(type);
	}

	@SuppressWarnings("unchecked")
	private <S extends T> FieldToken<O, S> withTypeCapture(Type type) {
		if (type == null)
			return (FieldToken<O, S>) this;

		BoundSet bounds = new ConstraintFormula(Kind.LOOSE_COMPATIBILILTY, fieldType.getType(), type).reduce(getBounds());

		TypeToken<? super O> receiverType = this.receiverType.withBounds(bounds);

		return new FieldToken<>(getMember(), receiverType);
	}

	@SuppressWarnings("unchecked")
	@Override
	public FieldToken<O, T> resolve() {
		return (FieldToken<O, T>) overField(getMember(), receiverType.resolve());
	}

	/**
	 * @param target
	 *          the instance to access the field of
	 * @return the value of the field
	 */
	@SuppressWarnings("unchecked")
	public T get(O target) {
		try {
			return (T) getMember().get(target);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new ReflectionException(p -> p.cannotGetField(target, this.getMember()), e);
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
			getMember().set(target, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new ReflectionException(p -> p.cannotSetField(target, value, this.getMember()), e);
		}
	}

	/**
	 * Find which fields are declared on this type.
	 * 
	 * @param declaringClass
	 *          the declaring class for which to retrieve the fields
	 * @return all {@link Field} objects applicable to this type, wrapped in
	 *         {@link FieldToken} instances
	 */
	public static FieldTokenQuery<FieldToken<Void, ?>, ?> staticFields(Class<?> declaringClass) {
		Stream<Field> fields = stream(declaringClass.getDeclaredFields()).filter(f -> Modifier.isStatic(f.getModifiers()));

		return fieldQuery(fields, FieldToken::overStaticField);
	}

	@Override
	public Optional<? extends DeclarationToken<?>> getOwningDeclaration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getTypeParameterCount() {
		return getDeclaringClass().getTypeParameters().length;
	}

	@Override
	public Stream<TypeParameter<?>> getTypeParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<TypeArgument<?>> getTypeArguments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FieldToken<O, T> withTypeArguments(Collection<? extends TypeArgument<?>> arguments) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isGeneric() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public FieldToken<O, T> withAllTypeArguments(List<Type> typeArguments) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FieldToken<O, T> withTypeArguments(List<Type> typeArguments) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FieldToken<O, T> withAllTypeArguments(Type... typeArguments) {
		return withAllTypeArguments(asList(typeArguments));
	}

	@Override
	public FieldToken<O, T> withTypeArguments(Type... typeArguments) {
		return withTypeArguments(asList(typeArguments));
	}
}

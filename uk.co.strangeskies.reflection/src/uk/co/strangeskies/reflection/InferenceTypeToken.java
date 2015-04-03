/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.reflection.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Set;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.utilities.function.collection.SetTransformOnceView;

public class InferenceTypeToken<T> extends TypeToken<T> {
	@SuppressWarnings("unchecked")
	private InferenceTypeToken(InferenceVariable type, Resolver resolver) {
		super(new Resolver(resolver), type, (Class<? super T>) Types
				.getRawType(IntersectionType.uncheckedFrom(resolver.getBounds()
						.getUpperBounds(type))));
	}

	@SuppressWarnings("unchecked")
	private InferenceTypeToken(Resolver resolver, WildcardType type) {
		super(resolver, resolver.inferWildcardType(type), (Class<? super T>) Types
				.getRawType(type));
	}

	public static InferenceTypeToken<?> of(InferenceVariable type,
			Resolver resolver) {
		return new InferenceTypeToken<>(type, resolver);
	}

	public static InferenceTypeToken<?> of(WildcardType bounds) {
		return new InferenceTypeToken<>(new Resolver(), bounds);
	}

	@Override
	public InferenceVariable getType() {
		return (InferenceVariable) super.getType();
	}

	public InferenceTypeToken<T> withLowerBound(Type type) {
		Resolver resolver = getResolver();
		resolver.addLowerBound(getType(), type);

		return new InferenceTypeToken<>(getType(), resolver);
	}

	public InferenceTypeToken<T> withUpperBound(Type type) {
		Resolver resolver = getResolver();
		resolver.addUpperBound(getType(), type);

		return new InferenceTypeToken<>(getType(), resolver);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <U> ParameterizedTypeToken<? extends U> resolveSupertypeParameters(
			Class<U> superclass) {
		if (!ParameterizedTypes.isGeneric(superclass))
			throw new IllegalArgumentException();

		Resolver resolver = getInternalResolver();

		resolver.capture(superclass);
		Type parameterizedType = resolver.resolveType(ParameterizedTypes.from(
				superclass).getType());

		new ConstraintFormula(Kind.SUBTYPE, getType(), parameterizedType)
				.reduceInto(resolver.getBounds());

		return (ParameterizedTypeToken<? extends U>) ParameterizedTypeToken
				.of((ParameterizedType) resolver.resolveType(parameterizedType));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <U> ParameterizedTypeToken<? extends U> resolveSubtypeParameters(
			Class<U> subclass) {
		if (!ParameterizedTypes.isGeneric(subclass))
			throw new IllegalArgumentException();

		Resolver resolver = getInternalResolver();

		resolver.capture(subclass);
		Type parameterizedType = resolver.resolveType(ParameterizedTypes.from(
				subclass).getType());

		new ConstraintFormula(Kind.SUBTYPE, parameterizedType, getType())
				.reduceInto(resolver.getBounds());

		return (ParameterizedTypeToken<? extends U>) ParameterizedTypeToken
				.of((ParameterizedType) resolver.resolveType(parameterizedType));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<? extends InferenceInvokable<? super T, ?>> getInvokables() {
		return new SetTransformOnceView<>(super.getInvokables(),
				InferenceInvokable::new);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<? extends InferenceInvokable<? super T, ?>> getMethods() {
		return new SetTransformOnceView<>(super.getMethods(),
				InferenceInvokable::new);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<? extends InferenceInvokable<T, T>> getConstructors() {
		return new SetTransformOnceView<>(super.getConstructors(),
				InferenceInvokable::new);
	}

	@Override
	public InferenceInvokable<? super T, ? extends T> resolveConstructorOverload(
			List<? extends Type> parameters) {
		return new InferenceInvokable<>(
				super.resolveConstructorOverload(parameters));
	}

	@Override
	public InferenceInvokable<? super T, ? extends T> resolveConstructorOverload(
			Type... parameters) {
		return new InferenceInvokable<>(
				super.resolveConstructorOverload(parameters));
	}

	@Override
	public InferenceInvokable<? super T, ?> resolveMethodOverload(String name,
			List<? extends Type> parameters) {
		return new InferenceInvokable<>(super.resolveMethodOverload(name,
				parameters));
	}

	@Override
	public InferenceInvokable<? super T, ?> resolveMethodOverload(String name,
			Type... parameters) {
		return new InferenceInvokable<>(super.resolveMethodOverload(name,
				parameters));
	}

	public static class InferenceInvokable<T, R> extends Invokable<T, R> {
		public InferenceInvokable(Invokable<T, R> invokable) {
			super(invokable);
		}

		@Override
		public InferenceTypeToken<T> getReceiverType() {
			return (InferenceTypeToken<T>) super.getReceiverType();
		}
	}
}

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
package uk.co.strangeskies.reflection.test;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.co.strangeskies.reflection.Invokable;
import uk.co.strangeskies.reflection.TypeLiteral;
import uk.co.strangeskies.reflection.TypeParameter;

public class TestTypeLiteral {
	public static class A<T> {
		public class B {}
	}

	public static class B {
		public <T extends Number> void method(T a, T b) {}

		public <T> void method(@SuppressWarnings("unchecked") Collection<T>... a) {}

		public <T> void bethod(Collection<T> a) {}

		public <T extends Number, U extends List<? super T>> Map<T, U> method2(
				List<T> a, U b) {
			return null;
		}

		public <T extends Number, U extends List<? super T>> U method(
				Collection<? extends T> a, U b) {
			return null;
		}

		public <T, R> void accept(Set<Invokable<T, R>> set) {}

		public strictfp <T extends Comparable<? super T>, U extends Collection<? extends Comparable<? super T>>> void bothways(
				T t, U u) {}

		public <U, R> Invokable<U, ? extends R> okay(
				Set<? extends Invokable<U, ? extends R>> candidates,
				List<? extends Type> parameters) {
			return null;
		}
	}

	public static <T> void main(String... args) throws NoSuchMethodException,
			SecurityException {
		System.out.println(new TypeParameter<T>() {});
		System.out.println(new TypeLiteral<List<String>>() {});
		System.out.println();

		System.out.println(new TypeLiteral<B>() {}.resolveMethodOverload("okay",
				new TypeLiteral<Set<Invokable<T, ?>>>() {}.getType(),
				new TypeLiteral<List<? extends Type>>() {}.getType()));
		System.out.println();

		System.out
				.println(TypeLiteral.from(B.class).resolveMethodOverload("bothways",
						String.class, new TypeLiteral<List<String>>() {}.getType()));
		System.out.println();

		System.out.println(TypeLiteral.from(Arrays.class).resolveMethodOverload(
				"asList", int.class, double.class));
		System.out.println();

		TypeLiteral.from(Arrays.class).resolveMethodOverload("asList", int.class,
				double.class);

		System.out.println(TypeLiteral.from(B.class).resolveMethodOverload(
				"method", new TypeLiteral<List<Integer>>() {}.getType(),
				new TypeLiteral<List<Number>>() {}.getType()));
		System.out.println();

		System.out.println(TypeLiteral.from(B.class).resolveMethodOverload(
				"method2", new TypeLiteral<List<Integer>>() {}.getType(),
				new TypeLiteral<List<Comparable<Integer>>>() {}.getType()));
		System.out.println();

		System.out.println(TypeLiteral.from(B.class).resolveMethodOverload(
				"method", new TypeLiteral<Collection<? super Integer>>() {}.getType()));
		System.out.println();

		System.out.println(TypeLiteral.from(B.class).resolveMethodOverload(
				"method",
				new TypeLiteral<Collection<? extends Integer>>() {}.getType(),
				new TypeLiteral<List<? super Number>>() {}.getType()));
		System.out.println();

		/*-
		System.out.println(TypeLiteral.from(B.class).resolveMethodOverload(
				"method", new TypeLiteral<Collection<? super Integer>>() {}.getType(),
				new TypeLiteral<Collection<? super Integer>>() {}.getType()));
		System.out.println();
		 */
	}
}

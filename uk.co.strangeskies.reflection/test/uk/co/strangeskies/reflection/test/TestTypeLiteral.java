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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.InferenceVariable;
import uk.co.strangeskies.reflection.Invokable;
import uk.co.strangeskies.reflection.ParameterizedTypes;
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

		public void moothod(Integer integer, Number number) {}

		public void moothod(Number integer, Integer number) {}

		public void moothod(Number integer, Number number) {}

		public <T extends Number, U extends List<? super T>> U method4(
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

		public static <T> T testeroonie(Class<T> t, String s) {
			return null;
		}
	}

	class Nest2<T extends Nest2<T>> {}

	class Nest22<T> extends Nest2<Nest22<T>> {}

	public static <H extends Nest2<H>> void main(String... args)
			throws NoSuchMethodException, SecurityException {
		System.out.println(new TypeParameter<H>() {});
		System.out.println(new TypeLiteral<List<String>>() {});
		System.out.println();

		System.out.println(ParameterizedTypes.from(HashSet.class,
				Arrays.asList(new BoundSet().createInferenceVariable()))
				.resolveSupertypeParameters(Collection.class));
		System.out.println();

		System.out.println(TypeLiteral
				.from(B.class)
				.resolveMethodOverload("bothways", String.class,
						new TypeLiteral<List<String>>() {}.getType()).infer());
		System.out.println();

		System.out.println(TypeLiteral.from(B.class).resolveMethodOverload(
				"moothod", Integer.class, Number.class));
		System.out.println();

		System.out.println(TypeLiteral.from(B.class).resolveMethodOverload(
				"moothod", Number.class, Integer.class));
		System.out.println();

		System.out.println(TypeLiteral.from(B.class).resolveMethodOverload(
				"moothod", Number.class, Number.class));
		System.out.println();

		/*-
		System.out.println(TypeLiteral.from(B.class).resolveMethodOverload(
				"moothod", Integer.class, Integer.class));
		System.out.println();
		 */

		System.out.println(TypeLiteral.from(Arrays.class)
				.resolveMethodOverload("asList", int.class, double.class)
				.withTargetType(new TypeLiteral<List<? extends Number>>() {}.getType())
				.infer());
		System.out.println();

		System.out.println(TypeLiteral
				.from(Arrays.class)
				.resolveMethodOverload("asList", int.class, double.class)
				.withTargetType(
						new TypeLiteral<List<? super Comparable<? extends Number>>>() {}
								.getType()).infer());
		System.out.println();

		System.out.println(TypeLiteral.from(B.class).resolveMethodOverload(
				"method", new TypeLiteral<List<Integer>>() {}.getType(),
				new TypeLiteral<List<Number>>() {}.getType()));
		System.out.println();

		System.out.println(TypeLiteral.from(B.class).resolveMethodOverload(
				"method2", new TypeLiteral<List<Integer>>() {}.getType(),
				new TypeLiteral<List<Comparable<Integer>>>() {}.getType()));
		System.out.println();

		System.out
				.println("<T extends Number, U extends List<? super T>> U method4(Collection<? extends T> a, U b)");
		System.out
				.println("B.method4((Collection<? extends Integer>) null, (List<? super Number>) null)");
		System.out.println(TypeLiteral
				.from(B.class)
				.resolveMethodOverload("method4",
						new TypeLiteral<Collection<? extends Integer>>() {}.getType(),
						new TypeLiteral<List<? super Number>>() {}.getType()).infer());
		System.out.println();

		System.out.println(TypeLiteral
				.from(B.class)
				.resolveMethodOverload("method",
						new TypeLiteral<Collection<? super Integer>>() {}.getType())
				.infer());
		System.out.println();

		System.out.println(new TypeLiteral<B>() {}.resolveMethodOverload("okay",
				new TypeLiteral<Set<Invokable<H, ?>>>() {}.getType(),
				new TypeLiteral<List<? extends Type>>() {}.getType()));
		System.out.println();

		/*
		 * System.out.println(TypeLiteral.from(B.class).resolveMethodOverload(
		 * "method", new TypeLiteral<Collection<? super Integer>>() {}.getType(),
		 * new TypeLiteral<Collection<? super Integer>>() {}.getType()));
		 * System.out.println();
		 */

		System.out.println(new TypeLiteral<Collection<H>>() {}
				.resolveSubtypeParameters(HashSet.class));
		System.out.println();

		System.out.println(new TypeLiteral<Nest2<H>>() {}
				.resolveSubtypeParameters(Nest22.class).getAllTypeArguments().values()
				.iterator().next() instanceof InferenceVariable);
		System.out.println();

		System.out.println(new TypeLiteral<B>() {}.resolveMethodOverload(
				"testeroonie", new TypeLiteral<Class<?>>() {}.getType(), String.class)
				.infer());
		System.out.println();

		TypeLiteral<?> targetClass = new TypeLiteral<List<?>>() {};
		TypeLiteral<?> resultClass = new TypeLiteral<Iterable<String>>() {};
		System.out.println(resultClass.isContainedBy(targetClass
				.resolveSupertypeParameters(resultClass.getRawType())));
	}
}

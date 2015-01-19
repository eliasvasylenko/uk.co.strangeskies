package uk.co.strangeskies.reflection.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

		public strictfp <T extends Comparable<? super T>, U extends Collection<? extends Comparable<? super T>>> void bothways(
				T t, U u) {}
	}

	public static <T> void main(String... args) throws NoSuchMethodException,
			SecurityException {
		System.out.println(new TypeParameter<T>() {});
		System.out.println(new TypeLiteral<List<String>>() {});
		System.out.println();

		System.out
				.println(TypeLiteral.from(B.class).resolveMethodOverload("bothways",
						String.class, new TypeLiteral<List<String>>() {}.getType()));
		System.out.println();

		System.out.println(TypeLiteral.from(Arrays.class).resolveMethodOverload(
				"asList", int.class, double.class));
		System.out.println();

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

		System.out.println(TypeLiteral.from(B.class).resolveMethodOverload(
				"method", new TypeLiteral<Collection<? super Integer>>() {}.getType(),
				new TypeLiteral<Collection<? super Integer>>() {}.getType()));
		System.out.println();
	}
}

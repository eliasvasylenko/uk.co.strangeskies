package uk.co.strangeskies.reflection.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import uk.co.strangeskies.reflection.Invokable;
import uk.co.strangeskies.reflection.TypeLiteral;
import uk.co.strangeskies.reflection.TypeParameter;

public class TestTypeLiteral {
	public static class A<T> {
		public class B {}
	}

	public static class B {
		public <T extends Number> void method(T a, T b) {}

		public <T extends Number, U extends List<? super T>> void method2(
				List<T> a, U b) {}

		public <T extends Number, U extends List<? super T>> void method(
				List<? extends T> a, U b) {}

		public strictfp <T extends Comparable<? super T>, U extends Collection<? extends Comparable<? super T>>> void bothways(
				T t, U u) {}
	}

	public static <T> void main(String... args) throws NoSuchMethodException,
			SecurityException {
		System.out.println(new TypeParameter<T>() {});
		System.out.println(new TypeLiteral<List<String>>() {});
		System.out.println();

		System.out.println("bothways");
		System.out.println(Invokable.of(
				B.class.getMethod("bothways", Comparable.class, Collection.class))
				.withLooseApplicability(String.class,
						new TypeLiteral<List<String>>() {}.getType()));
		System.out.println();

		System.out.println("aslist");
		System.out.println(Invokable.of(
				Arrays.class.getMethod("asList", Object[].class))
				.withVariableArityApplicability(int.class, double.class));
		System.out.println();

		System.out.println("method");
		System.out.println(Invokable.of(
				B.class.getMethod("method", List.class, List.class))
				.withLooseApplicability(new TypeLiteral<List<Integer>>() {}.getType(),
						new TypeLiteral<List<Number>>() {}.getType()));
		System.out.println();

		System.out.println("method2");
		System.out.println(Invokable.of(
				B.class.getMethod("method2", List.class, List.class))
				.withLooseApplicability(new TypeLiteral<List<Integer>>() {}.getType(),
						new TypeLiteral<List<Comparable<Integer>>>() {}.getType()));
		System.out.println();

		System.out.println("method");
		System.out.println(Invokable.of(
				B.class.getMethod("method", Number.class, Number.class))
				.withLooseApplicability(Integer.class, Double.class));
		System.out.println();
	}
}

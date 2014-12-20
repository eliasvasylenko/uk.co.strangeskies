package uk.co.strangeskies.reflection.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import uk.co.strangeskies.reflection.TypeLiteral;
import uk.co.strangeskies.reflection.impl.ApplicabilityVerifier;

import com.google.common.reflect.Invokable;

public class TestTypeLiteral {
	public class A<T> {
		public class B {}
	}

	public class B {
		public <T extends Number> void method(T a, T b) {}

		public <T extends Number, U extends List<? super T>> void method2(
				List<T> a, U b) {}

		public <T extends Number, U extends List<? super T>> void method(
				List<? extends T> a, U b) {}

		public <T extends Comparable<? super T>, U extends Collection<? extends Comparable<? super T>>> void bothways(
				T t, U u) {}
	}

	public static void main(String... args) throws NoSuchMethodException,
			SecurityException {
		Arrays.asList(1, 2.0);

		System.out.println(new ApplicabilityVerifier(Invokable.from(Arrays.class
				.getMethod("asList", Object[].class)), null, int.class, double.class)
				.verifyVariableArityParameterApplicability());

		System.out.println(new ApplicabilityVerifier(Invokable.from(B.class
				.getMethod("bothways", Comparable.class, Collection.class)), null,
				String.class, new TypeLiteral<List<String>>() {}.getType())
				.verifyLooseParameterApplicability());

		System.out.println(new ApplicabilityVerifier(Invokable.from(B.class
				.getMethod("method", List.class, List.class)), null,
				new TypeLiteral<List<Integer>>() {}.getType(),
				new TypeLiteral<List<Number>>() {}.getType())
				.verifyLooseParameterApplicability());

		System.out.println(new ApplicabilityVerifier(Invokable.from(B.class
				.getMethod("method2", List.class, List.class)), null,
				new TypeLiteral<List<Integer>>() {}.getType(),
				new TypeLiteral<List<Integer>>() {}.getType())
				.verifyLooseParameterApplicability());

		System.out.println(new ApplicabilityVerifier(Invokable.from(B.class
				.getMethod("method", Number.class, Number.class)), null, Integer.class,
				Double.class).verifyLooseParameterApplicability());
	}
}

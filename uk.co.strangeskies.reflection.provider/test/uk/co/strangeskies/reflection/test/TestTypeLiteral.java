package uk.co.strangeskies.reflection.test;

import java.util.List;

import uk.co.strangeskies.reflection.TypeLiteral;
import uk.co.strangeskies.reflection.impl.ApplicabilityVerification;

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
	}

	public static void main(String... args) throws NoSuchMethodException,
			SecurityException {
		System.out.println(new ApplicabilityVerification(Invokable.from(B.class
				.getMethod("method", List.class, List.class)), null,
				new TypeLiteral<List<Integer>>() {}.type(),
				new TypeLiteral<List<? super Integer>>() {}.type())
				.verifyLooseParameterApplicability());

		System.out.println(new ApplicabilityVerification(Invokable.from(B.class
				.getMethod("method2", List.class, List.class)), null,
				new TypeLiteral<List<Integer>>() {}.type(),
				new TypeLiteral<List<Integer>>() {}.type())
				.verifyLooseParameterApplicability());

		System.out.println(new ApplicabilityVerification(Invokable.from(B.class
				.getMethod("method", Number.class, Number.class)), null, Integer.class,
				Double.class).verifyLooseParameterApplicability());

		System.out.println(new ApplicabilityVerification(Invokable.from(B.class
				.getMethod("method", Number.class, Number.class)), null, String.class,
				String.class).verifyLooseParameterApplicability());
	}
}

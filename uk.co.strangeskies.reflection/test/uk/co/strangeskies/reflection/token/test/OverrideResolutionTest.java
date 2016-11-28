package uk.co.strangeskies.reflection.token.test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static uk.co.strangeskies.reflection.token.TypeToken.overType;

import org.junit.Test;

import uk.co.strangeskies.reflection.token.ExecutableToken;

interface A {
	Object method();
}

interface B {
	Object method();
}

interface C extends A, B {
	@Override
	Number method();
}

abstract class D implements A {}

abstract class E extends D implements C {}

abstract class F implements C {
	@Override
	public abstract Integer method();
}

public class OverrideResolutionTest {
	public static final String METHOD_NAME = "method";

	@Test
	public void withReceiverTypeDoesNotOverride() throws NoSuchMethodException, SecurityException {
		ExecutableToken<? extends A, ?> method = overType(A.class).methods().named(METHOD_NAME).resolveOverload();

		assertThat(method.getMember(), equalTo(A.class.getMethod(METHOD_NAME)));

		method = method.withReceiverType(C.class);

		assertThat(method.getMember(), equalTo(A.class.getMethod(METHOD_NAME)));
	}

	@Test
	public void withReceiverThenGetOverride() throws NoSuchMethodException, SecurityException {
		ExecutableToken<? extends A, ?> method = overType(A.class).methods().named(METHOD_NAME).resolveOverload();

		method = method.withReceiverType(C.class).getOverride().get();

		assertThat(method.getMember(), equalTo(C.class.getMethod(METHOD_NAME)));
	}

	@Test
	public void getOverride() throws NoSuchMethodException, SecurityException {
		ExecutableToken<? extends A, ?> method = ExecutableToken
				.overMethod(A.class.getMethod(METHOD_NAME), overType(C.class));

		assertThat(method.getMember().getDeclaringClass(), equalTo(A.class));

		method = method.getOverride().get();

		assertThat(method.getMember().getDeclaringClass(), equalTo(C.class));
	}

	@Test
	public void getIndirectOverride() throws NoSuchMethodException, SecurityException {
		ExecutableToken<? extends A, ?> method = ExecutableToken
				.overMethod(A.class.getMethod(METHOD_NAME), overType(F.class));

		assertThat(method.getMember().getDeclaringClass(), equalTo(A.class));

		method = method.getOverride().get();

		assertThat(method.getMember().getDeclaringClass(), equalTo(F.class));
	}

	@Test
	public void getInterfaceBeforeClassOverride() throws NoSuchMethodException, SecurityException {
		ExecutableToken<? extends A, ?> method = ExecutableToken
				.overMethod(A.class.getMethod(METHOD_NAME), overType(E.class));

		assertThat(method.getMember().getDeclaringClass(), equalTo(A.class));

		method = method.getOverride().get();

		assertThat(method.getMember().getDeclaringClass(), equalTo(C.class));
	}
}

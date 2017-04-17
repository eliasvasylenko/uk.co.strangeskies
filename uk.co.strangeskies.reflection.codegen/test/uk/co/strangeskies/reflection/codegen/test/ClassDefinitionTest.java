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
 * This file is part of uk.co.strangeskies.reflection.codegen.
 *
 * uk.co.strangeskies.reflection.codegen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection.codegen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.codegen.test;

import static uk.co.strangeskies.reflection.codegen.ClassSignature.classSignature;
import static uk.co.strangeskies.reflection.codegen.LiteralExpression.literal;
import static uk.co.strangeskies.reflection.codegen.MethodSignature.methodSignature;
import static uk.co.strangeskies.reflection.codegen.ParameterSignature.parameterSignature;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.codegen.Block;
import uk.co.strangeskies.reflection.codegen.ClassDefinition;
import uk.co.strangeskies.reflection.codegen.ClassSignature;
import uk.co.strangeskies.reflection.codegen.Expressions;
import uk.co.strangeskies.reflection.codegen.MethodDeclaration;
import uk.co.strangeskies.reflection.codegen.MethodSignature;
import uk.co.strangeskies.reflection.codegen.ParameterSignature;
import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.TypeToken;

@SuppressWarnings("javadoc")
public class ClassDefinitionTest {
	private interface Func<A, B> {
		B apply(A value);
	}

	private interface Default {
		@SuppressWarnings("unused")
		default void method() {}
	}

	private static final ClassSignature<?> TEST_CLASS_SIGNATURE = classSignature()
			.withPackageName(ClassDeclarationTest.class.getPackage().getName())
			.withSimpleName("SelfSet");

	private static final TypeToken<String> STRING_TYPE = new TypeToken<String>() {};

	@Test
	public void defineObject() {
		Object instance = TEST_CLASS_SIGNATURE.defineStandalone().instantiateReflectively().cast();

		instance.hashCode();
	}

	@Test
	public void runnableClassInvocation() {
		ClassDefinition<Void, ? extends Runnable> classDefinition = TEST_CLASS_SIGNATURE
				.withSuperType(Runnable.class)
				.defineStandalone()
				.withMethodDefinition(methodSignature("run"), new Block<Void>().withReturnStatement());

		Runnable instance = classDefinition.instantiateReflectively().cast();

		instance.run();
	}

	private ExecutableToken<String, String> concatMethod() {
		return STRING_TYPE.methods().named("concat").resolveOverload(STRING_TYPE).withTargetType(STRING_TYPE);
	}

	@Test
	public void defineWithExplicitMethodDeclaration() {
		ParameterSignature<String> applyParameter = parameterSignature("value", STRING_TYPE);
		MethodSignature<String> applyMethod = methodSignature("apply").withReturnType(STRING_TYPE).withParameters(
				applyParameter);

		/*
		 * A block is something we don't always know the type of until we resolve
		 * the type of it's context, e.g. in method overload resolution a lambda
		 * body must be reinterpreted according to all applicable target types, and
		 * any return statements examined according to that context.
		 * 
		 * Do we achieve this with a new kind of stand-in type? Or do existing
		 * models already fit well (e.g. some sort of ad-hoc type variable)?
		 */

		Func<String, String> instance = TEST_CLASS_SIGNATURE
				.withSuperType(new TypeToken<Func<String, String>>() {})
				.withMethod(applyMethod)
				.defineStandalone()
				.withMethodDefinition(
						applyMethod,
						d -> new Block<String>()
								.withReturnStatement(d.getParameter(applyParameter).invokeMethod(concatMethod(), literal("append"))))
				.instantiateReflectively()
				.cast();

		String result = instance.apply("string");

		Assert.assertEquals("stringappend", result);
	}

	// @Test
	public void defineWithInheritedMethodDeclarationBySignature() {
		ParameterSignature<String> applyParameter = parameterSignature("value", STRING_TYPE);

		Func<String, String> instance = TEST_CLASS_SIGNATURE
				.withSuperType(new TypeToken<Func<String, String>>() {})
				.defineStandalone()
				.withMethodDefinition(
						methodSignature("apply").withReturnType(String.class).withParameters(applyParameter),
						d -> new Block<String>().withReturnStatement(
								d
										.getParameter(applyParameter)
										.invokeMethod(concatMethod(), d.getParameter(parameterSignature("", Integer.class)))))
				.instantiateReflectively()
				.cast();

		String result = instance.apply("string");

		Assert.assertEquals("stringstring", result);
	}

	// @Test
	public void defineWithInheritedMethodDeclaration() {
		defineFunctionClass(
				TEST_CLASS_SIGNATURE.withSuperType(new TypeToken<Func<String, String>>() {}).defineStandalone());
	}

	private <F extends Func<String, String>> void defineFunctionClass(ClassDefinition<Void, F> classDefinition) {
		ParameterSignature<String> applyParameter = parameterSignature("value", STRING_TYPE);
		MethodDeclaration<F, String> applyMethod = classDefinition.getDeclaration().getMethodDeclaration(
				methodSignature("apply").withReturnType(String.class).withParameters(applyParameter));

		Func<String, String> instance = classDefinition
				.withMethodDefinition(
						applyMethod,
						d -> new Block<String>().withReturnStatement(
								d.getParameter(applyParameter).invokeMethod(concatMethod(), d.getParameter(applyParameter))))
				.instantiateReflectively()
				.cast();

		String result = instance.apply("string");

		Assert.assertEquals("stringstring", result);
	}

	@Test(expected = ReflectionException.class)
	public void defineWithAbstractMethod() {
		TEST_CLASS_SIGNATURE.withSuperType(Runnable.class).defineStandalone().instantiateReflectively();
	}

	@Test
	public void defineWithDefaultMethod() {
		TEST_CLASS_SIGNATURE.withSuperType(Default.class).defineStandalone().instantiateReflectively();
	}
}

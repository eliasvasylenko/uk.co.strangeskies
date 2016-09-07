/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
package uk.co.strangeskies.reflection.test;

import static uk.co.strangeskies.reflection.AssignmentExpressionDefinition.assign;
import static uk.co.strangeskies.reflection.FieldExpressionDefinition.field;
import static uk.co.strangeskies.reflection.LiteralExpressionDefinition.literal;
import static uk.co.strangeskies.reflection.MethodExpressionDefinition.invoke;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.reflection.FieldMember;
import uk.co.strangeskies.reflection.InvocableMember;
import uk.co.strangeskies.reflection.Scope;
import uk.co.strangeskies.reflection.State;
import uk.co.strangeskies.reflection.TypeToken;

public class ExpressionTest {
	public class TestClass {
		public String field;

		public void setMethod(String value) {
			field = value;
		}

		public String getMethod() {
			return field;
		}
	}

	private static final TypeToken<TestClass> TEST_CLASS_TYPE = new TypeToken<TestClass>() {};
	private static final String TEST_FIELD_NAME = "field";
	@SuppressWarnings("unchecked")
	private static final FieldMember<TestClass, String> TEST_FIELD = (FieldMember<TestClass, String>) TEST_CLASS_TYPE
			.resolveField(TEST_FIELD_NAME);

	private static final String TEST_SET_METHOD_NAME = "setMethod";
	@SuppressWarnings("unchecked")
	private static final InvocableMember<TestClass, ?> TEST_SET_METHOD = (InvocableMember<TestClass, Void>) TEST_CLASS_TYPE
			.resolveMethodOverload(TEST_SET_METHOD_NAME, String.class);

	@Test
	public void thisAssignmentTest() {
		TestClass testInstance = new TestClass();

		Scope<TestClass> scope = new Scope<>(TEST_CLASS_TYPE);
		State state = new State() {
			@Override
			public Scope<?> getScope() {
				return scope;
			}

			@Override
			public <I> I getEnclosingInstance(Scope<I> parentScope) {
				return (I) testInstance;
			}
		};

		assign(field(scope.receiver(), TEST_FIELD), literal("value")).evaluate(state);

		Assert.assertEquals("value", testInstance.field);
	}

	@Test
	public void methodInvocationTest() {
		TestClass testInstance = new TestClass();

		Scope<TestClass> scope = new Scope<>(TEST_CLASS_TYPE);
		State state = new State() {
			@Override
			public Scope<?> getScope() {
				return scope;
			}

			@Override
			public <I> I getEnclosingInstance(Scope<I> parentScope) {
				return (I) testInstance;
			}
		};

		invoke(scope.receiver(), TEST_SET_METHOD, literal("value")).evaluate(state);

		Assert.assertEquals("value", testInstance.field);
	}
}

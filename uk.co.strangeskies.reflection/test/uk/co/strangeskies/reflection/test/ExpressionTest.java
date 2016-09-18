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

import static uk.co.strangeskies.reflection.LiteralExpression.literal;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.reflection.FieldMember;
import uk.co.strangeskies.reflection.InvocableMember;
import uk.co.strangeskies.reflection.State;
import uk.co.strangeskies.reflection.StaticScope;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.ValueExpression;
import uk.co.strangeskies.reflection.ValueResult;
import uk.co.strangeskies.reflection.VariableExpression;

@SuppressWarnings("javadoc")
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

	private static final TypeToken<String> STRING_TYPE = new TypeToken<String>() {};

	private static final TypeToken<TestClass> TEST_CLASS_TYPE = new TypeToken<TestClass>() {};
	private static final String TEST_FIELD_NAME = "field";
	@SuppressWarnings("unchecked")
	private static final FieldMember<TestClass, String> TEST_FIELD = (FieldMember<TestClass, String>) TEST_CLASS_TYPE
			.resolveField(TEST_FIELD_NAME);

	private static final String TEST_SET_METHOD_NAME = "setMethod";
	@SuppressWarnings("unchecked")
	private static final InvocableMember<TestClass, ?> TEST_SET_METHOD = (InvocableMember<TestClass, Void>) TEST_CLASS_TYPE
			.resolveMethodOverload(TEST_SET_METHOD_NAME, STRING_TYPE);

	private static ValueExpression<TestClass> valueExpression(TestClass instance) {
		return new ValueExpression<TestClass>() {
			@Override
			public ValueResult<TestClass> evaluate(State state) {
				return () -> instance;
			}

			@Override
			public TypeToken<TestClass> getType() {
				return TEST_CLASS_TYPE;
			}
		};
	}

	@Test
	public void fieldAssignmentTest() {
		StaticScope scope = StaticScope.create();
		VariableExpression<TestClass> variable = scope.declareVariable(TestClass.class);

		State state = scope.initializeState();
		TestClass instance = new TestClass();

		variable.assign(valueExpression(instance)).evaluate(state);
		variable.accessField(TEST_FIELD).assign(literal("value")).evaluate(state);

		Assert.assertEquals("value", instance.field);
	}

	@Test
	public void localAssignmentTest() {
		StaticScope scope = StaticScope.create();
		VariableExpression<String> local = scope.declareVariable(STRING_TYPE);

		State state = scope.initializeState();

		local.assign(literal("value")).evaluate(state);

		Assert.assertEquals("value", local.evaluate(state).get());
	}

	@Test
	public void localMethodInvocationTest() {
		StaticScope scope = StaticScope.create();
		VariableExpression<TestClass> variable = scope.declareVariable(TestClass.class);

		State state = scope.initializeState();
		TestClass instance = new TestClass();

		variable.assign(valueExpression(instance)).evaluate(state);
		variable.invokeMethod(TEST_SET_METHOD, literal("value")).evaluate(state);

		Assert.assertEquals("value", instance.field);
	}
}

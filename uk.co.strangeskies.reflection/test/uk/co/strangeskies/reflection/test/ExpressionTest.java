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
import uk.co.strangeskies.reflection.DefinitionVisitor;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.ValueExpression;
import uk.co.strangeskies.reflection.ValueResult;
import uk.co.strangeskies.reflection.VariableExpression;
import uk.co.strangeskies.reflection.VariableResult;
import uk.co.strangeskies.reflection.VoidBlock;

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
			public ValueResult<TestClass> evaluate(DefinitionVisitor state) {
				return () -> instance;
			}

			@Override
			public TypeToken<TestClass> getType() {
				return TEST_CLASS_TYPE;
			}
		};
	}

	private static VariableExpression<Object> resultExpression() {
		return new VariableExpression<Object>() {
			private Object value;

			@Override
			public TypeToken<Object> getType() {
				return new TypeToken<Object>() {};
			}

			public Object getValue() {
				return value;
			}

			public void setValue(Object value) {
				this.value = value;
			}

			@Override
			public VariableResult<Object> evaluate(DefinitionVisitor state) {
				return new VariableResult<Object>() {
					@Override
					public Object get() {
						return getValue();
					}

					@Override
					public void set(Object value) {
						setValue(value);
					}
				};
			}
		};
	}

	@Test
	public void fieldAssignmentTest() {
		TestClass instance = new TestClass();

		VoidBlock block = new VoidBlock();
		VariableExpression<TestClass> variable = block.declareVariable(TestClass.class, valueExpression(instance));

		block.addExpression(variable.accessField(TEST_FIELD).assign(literal("value")));
		block.execute(DefinitionVisitor.create());

		Assert.assertEquals("value", instance.field);
	}

	@Test
	public void localAssignmentTest() {
		VoidBlock block = new VoidBlock();
		VariableExpression<String> local = block.declareVariable(STRING_TYPE);
		VariableExpression<Object> result = resultExpression();

		block.addExpression(local.assign(literal("value")));
		block.addExpression(result.assign(local));
		block.execute(DefinitionVisitor.create());

		Assert.assertEquals("value", result.evaluate(null).get());
	}

	@Test
	public void localMethodInvocationTest() {
		VoidBlock block = new VoidBlock();
		VariableExpression<TestClass> variable = block.declareVariable(TestClass.class);

		TestClass instance = new TestClass();

		block.addExpression(variable.assign(valueExpression(instance)));
		block.addExpression(variable.invokeMethod(TEST_SET_METHOD, literal("value")));
		block.execute(DefinitionVisitor.create());

		Assert.assertEquals("value", instance.field);
	}
}

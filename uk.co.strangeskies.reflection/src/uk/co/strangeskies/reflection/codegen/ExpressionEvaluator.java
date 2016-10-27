/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
package uk.co.strangeskies.reflection.codegen;

import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.codegen.ExpressionVisitor.ValueExpressionVisitor;
import uk.co.strangeskies.reflection.codegen.ExpressionVisitor.VariableExpressionVisitor;
import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.FieldToken;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ExpressionEvaluator {
	private class ExpressionVisitorImpl implements ExpressionVisitor {
		@Override
		public <U> ValueExpressionVisitor<U> value(TypeToken<U> type) {
			return new ValueExpressionVisitorImpl<>(type);
		}
	}

	private class ValueExpressionVisitorImpl<T> implements ValueExpressionVisitor<T> {
		private final TypeToken<T> type;
		private boolean complete = false;
		private ValueResult<T> result;

		public ValueExpressionVisitorImpl(TypeToken<T> type) {
			this.type = type;
		}

		private ValueResult<T> getResult() {
			if (!complete) {
				throw new CodeGenerationException(p -> p.incompleteExpressionEvaluation());
			}
			return result;
		}

		private void complete(ValueResult<T> result) {
			complete = true;
			this.result = result;
		}

		@Override
		public VariableExpressionVisitorImpl<T> variable() {
			return new VariableExpressionVisitorImpl<>(this);
		}

		@Override
		public void visitAssignment(VariableExpression<T> target, ValueExpression<? extends T> value) {
			T result = evaluate(value).get();
			evaluate(target).set(result);
			complete(() -> result);
		}

		@Override
		public void visitLiteral(T value) {
			complete(() -> value);
		}

		@Override
		public void visitNull() {
			complete(null);
		}

		@Override
		public void visitReceiver(ClassDefinition<T> classDefinition) {
			complete(() -> state.getEnclosingInstance(classDefinition));
		}

		@Override
		public <O> void visitMethod(ValueExpression<O> receiver, ExecutableToken<? super O, T> invocable,
				List<ValueExpression<?>> arguments) {
			O targetObject = evaluate(receiver).get();

			T result = invocable.invoke(targetObject,
					arguments.stream().map(a -> evaluate(a).get()).collect(Collectors.toList()));

			complete(() -> result);
		}

		@Override
		public void visitLocal(LocalVariable<? extends T> local) {
			complete(() -> state.getEnclosedLocal(local));
		}
	}

	private class VariableExpressionVisitorImpl<T> implements VariableExpressionVisitor<T> {
		private final ValueExpressionVisitorImpl<T> parent;

		public VariableExpressionVisitorImpl(ValueExpressionVisitorImpl<T> parent) {
			this.parent = parent;
		}

		private VariableResult<T> getResult() {
			return (VariableResult<T>) parent.getResult();
		}

		private void complete(VariableResult<T> result) {
			parent.complete(result);
		}

		@Override
		public <O> void visitField(ValueExpression<? extends O> value, FieldToken<O, T> field) {
			O targetObject = evaluate(value).get();

			complete(new VariableResult<T>() {
				@Override
				public T get() {
					return field.get(targetObject);
				}

				@Override
				public void set(T value) {
					field.set(targetObject, value);
				}
			});
		}

		@Override
		public void visitLocal(LocalVariable<T> local) {
			complete(new VariableResult<T>() {
				@Override
				public T get() {
					return state.getEnclosedLocal(local);
				}

				@Override
				public void set(T value) {
					state.setEnclosedLocal(local, value);
				}
			});
		}
	}

	private final ExpressionVisitorImpl expressionVisitor = new ExpressionVisitorImpl();
	private final StatementExecutor state;

	public ExpressionEvaluator(StatementExecutor state) {
		this.state = state;
	}

	public synchronized void evaluate(Expression expression) {
		expression.accept(expressionVisitor);
	}

	public synchronized <T> ValueResult<T> evaluate(ValueExpression<T> expression) {
		ValueExpressionVisitorImpl<T> visitor = new ValueExpressionVisitorImpl<>(expression.getType());

		expression.accept(visitor);

		return visitor.getResult();
	}

	public synchronized <T> VariableResult<T> evaluate(VariableExpression<T> expression) {
		VariableExpressionVisitorImpl<T> visitor = new ValueExpressionVisitorImpl<>(expression.getType()).variable();

		expression.accept(visitor);

		return visitor.getResult();
	}
}

package uk.co.strangeskies.reflection;

import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.ExpressionVisitor.ValueExpressionVisitor;
import uk.co.strangeskies.reflection.ExpressionVisitor.VariableExpressionVisitor;

public class Evaluator {
	private class ExpressionVisitorImpl implements ExpressionVisitor {
		@Override
		public <U> ValueExpressionVisitor<U> value(TypeToken<U> type) {
			return new ValueExpressionVisitorImpl<>(type);
		}
	}

	private class ValueExpressionVisitorImpl<T> implements ValueExpressionVisitor<T> {
		private final TypeToken<T> type;

		public ValueExpressionVisitorImpl(TypeToken<T> type) {
			this.type = type;
		}

		@Override
		public VariableExpressionVisitor<T> variable() {
			return new VariableExpressionVisitorImpl<>(type);
		}

		@Override
		public void visitAssignment(VariableExpression<T> target, ValueExpression<? extends T> value) {
			// TODO Auto-generated method stub

		}

		@Override
		public void visitLiteral(T value) {
			// TODO Auto-generated method stub

		}

		@Override
		public void visitNull() {
			// TODO Auto-generated method stub

		}

		@Override
		public void visitReceiver(ClassDefinition<T> classDefinition) {
			// TODO Auto-generated method stub

		}

		@Override
		public <O> void visitMethod(ValueExpression<O> receiver, InvocableMember<? super O, T> invocable,
				List<ValueExpression<?>> arguments) {
			O targetObject = evaluate(receiver).get();

			T result = invocable.invoke(targetObject,
					arguments.stream().map(a -> evaluate(a).get()).collect(Collectors.toList()));
		}

		@Override
		public void visitLocal(LocalVariable<? extends T> local) {
			new ValueResult<T>() {
				@Override
				public T get() {
					return state.getEnclosedLocal(local);
				}
			};
		}
	}

	private class VariableExpressionVisitorImpl<T> implements VariableExpressionVisitor<T> {
		private final TypeToken<T> type;

		public VariableExpressionVisitorImpl(TypeToken<T> type) {
			this.type = type;
		}

		@Override
		public <O> void visitField(ValueExpression<? extends O> value, FieldMember<O, T> field) {
			O targetObject = evaluate(value).get();

			new VariableResult<T>() {
				@Override
				public T get() {
					return field.get(targetObject);
				}

				@Override
				public void set(T value) {
					field.set(targetObject, value);
				}
			};
		}

		@Override
		public void visitLocal(LocalVariable<T> local) {
			new VariableResult<T>() {
				@Override
				public T get() {
					return state.getEnclosedLocal(local);
				}

				@Override
				public void set(T value) {
					state.setEnclosedLocal(local, value);
				}
			};
		}
	}

	private final ExpressionVisitorImpl expressionVisitor = new ExpressionVisitorImpl();
	private final Executor state;

	public Evaluator(Executor state) {
		this.state = state;
	}

	public void evaluate(Expression expression) {
		expression.accept(expressionVisitor);
	}

	public <T> ValueResult<T> evaluate(ValueExpression<T> expression) {
		expression.accept(expressionVisitor);

		return null;
	}

	public <T> VariableResult<T> evaluate(VariableExpression<T> expression) {
		expression.accept(expressionVisitor);

		return null;
	}
}

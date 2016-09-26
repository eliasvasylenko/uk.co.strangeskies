package uk.co.strangeskies.reflection;

import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.ValueExpressionVisitor.Vis;

public class Evaluator {
	private class ExpressionVisitorImpl implements ExpressionVisitor<Object> {
		@Override
		public <V> Vis<? extends Object, V> visitValue(TypeToken<V> type) {
			return new ValueExpressionVisitorImpl<>(type);
		}
	}

	private class ValueExpressionVisitorImpl<T> implements Vis<ValueResult<T>, T> {
		private final TypeToken<T> type;
		private VariableExpressionVisitorImpl<T> variable;

		public ValueExpressionVisitorImpl(TypeToken<T> type) {
			this.type = type;
		}

		@Override
		public <V extends T> ValueResult<T> visitVariable(VariableExpression<V> variable) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <V extends T, O> ValueResult<T> visitField(ValueExpression<? extends O> value, FieldMember<O, V> field) {
			return variable().visitField(value, field);
		}

		@Override
		public <V extends T, W extends V> ValueResult<T> visitAssignment(VariableExpression<V> target,
				ValueExpression<W> value) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <V extends T> ValueResult<T> visitLiteral(V value, TypeToken<V> type) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ValueResult<T> visitNull() {
			return null;
		}

		@Override
		public <V extends T> ValueResult<T> visitReceiver(ClassDefinition<V> classDefinition) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <V extends T, O> ValueResult<T> visitMethod(ValueExpression<O> receiver,
				InvocableMember<? super O, V> invocable, List<ValueExpression<?>> arguments) {
			O targetObject = evaluate(receiver).get();

			T result = invocable.invoke(targetObject,
					arguments.stream().map(a -> evaluate(a).get()).collect(Collectors.toList()));

			return () -> result;
		}
	}

	private class VariableExpressionVisitorImpl<T> implements VariableExpressionVisitor<VariableResult<T>, T> {
		private final TypeToken<T> type;

		public VariableExpressionVisitorImpl(TypeToken<T> type) {
			this.type = type;
		}

		@Override
		public <V extends T, O> VariableResult<T> visitField(ValueExpression<? extends O> value, FieldMember<O, V> field) {
			O targetObject = evaluate(value).get();

			return new VariableResult<T>() {
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
	}

	private final ExpressionVisitorImpl expressionVisitor = new ExpressionVisitorImpl();

	public void evaluate(Expression expression) {
		expression.accept(expressionVisitor);
	}

	@SuppressWarnings("unchecked")
	public <T> ValueResult<T> evaluate(ValueExpression<T> expression) {
		return expression.accept(expressionVisitor.value(expression.getType()));
	}

	@SuppressWarnings("unchecked")
	public <T> VariableResult<T> evaluate(VariableExpression<T> expression) {
		return expression.accept(expressionVisitor.value(expression.getType()).variable());
	}
}

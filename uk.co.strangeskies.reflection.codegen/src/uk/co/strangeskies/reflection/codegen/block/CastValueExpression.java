package uk.co.strangeskies.reflection.codegen.block;

import uk.co.strangeskies.reflection.codegen.block.ExpressionVisitor.ValueExpressionVisitor;
import uk.co.strangeskies.reflection.token.TypeToken;

public class CastValueExpression<T> implements ValueExpression<T> {
	public CastValueExpression(TypeToken<T> token, ValueExpression<?> value) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void accept(ValueExpressionVisitor<T> visitor) {
		// TODO Auto-generated method stub

	}
}

package uk.co.strangeskies.reflection.codegen;

import static uk.co.strangeskies.reflection.codegen.ClassDeclaration.declareClass;
import static uk.co.strangeskies.reflection.codegen.InvocationExpression.invokeStatic;

import java.util.concurrent.atomic.AtomicLong;

import uk.co.strangeskies.reflection.TypeToken;

public class Expressions {
	private Expressions() {}

	private static final AtomicLong TYPE_TOKEN_EXPRESSION_COUNT = new AtomicLong(0);

	public <T> ValueExpression<? extends TypeToken<T>> typeTokenExpression(TypeToken<T> type) {
		ClassDefinition<? extends TypeToken<T>> typeTokenClass = declareClass(
				"TypeTokenExpression$" + TYPE_TOKEN_EXPRESSION_COUNT.incrementAndGet()).withSuperType(type.getThisType())
						.define();

		return invokeStatic(typeTokenClass.declareConstructor().define().asToken());
	}
}

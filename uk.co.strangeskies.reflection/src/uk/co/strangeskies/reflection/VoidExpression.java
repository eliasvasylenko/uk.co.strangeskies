package uk.co.strangeskies.reflection;

public interface VoidExpression extends Expression {
	@Override
	VoidResult evaluate(EvaluationScope context);
}

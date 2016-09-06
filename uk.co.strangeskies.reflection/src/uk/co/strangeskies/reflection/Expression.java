package uk.co.strangeskies.reflection;

public interface Expression {
	Result evaluate(EvaluationScope context);
}

package uk.co.strangeskies.reflection.codegen.block;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.reflection.codegen.block.Expressions.invokeStatic;
import static uk.co.strangeskies.reflection.codegen.block.Expressions.literal;
import static uk.co.strangeskies.reflection.token.ExecutableToken.staticMethods;
import static uk.co.strangeskies.reflection.token.MethodMatcher.allMethods;
import static uk.co.strangeskies.reflection.token.OverloadResolver.resolveOverload;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.codegen.block.ExpressionVisitor.ValueExpressionVisitor;
import uk.co.strangeskies.reflection.token.ExecutableToken;

class AnnotatedTypeExpression implements ValueExpression<AnnotatedType> {
	@SuppressWarnings("unchecked")
	private static final ExecutableToken<Void, AnnotatedType> ANNOTATED_METHOD = (ExecutableToken<Void, AnnotatedType>) staticMethods(
			AnnotatedTypes.class)
					.filter(allMethods().named("annotated"))
					.collect(resolveOverload(Type.class, Annotation[].class))
					.asVariableArityInvocation();

	private final ValueExpression<AnnotatedType> expression;

	public AnnotatedTypeExpression(AnnotatedType annotatedType) {
		Type type = annotatedType.getType();

		if (type instanceof Class<?>) {
			List<ValueExpression<?>> arguments = new ArrayList<>();
			arguments.add(literal((Class<?>) type));
			arguments.addAll(getAnnotationExpressions(annotatedType.getAnnotations()));
			expression = invokeStatic(ANNOTATED_METHOD, arguments);
		}

		throw new IllegalArgumentException();
	}

	private List<ValueExpression<Annotation>> getAnnotationExpressions(Annotation[] annotations) {
		return stream(annotations).map(AnnotationExpression::new).collect(toList());
	}

	@Override
	public void accept(ValueExpressionVisitor<AnnotatedType> visitor) {
		expression.accept(visitor);
	}
}

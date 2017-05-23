package uk.co.strangeskies.reflection.codegen.block;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.reflection.codegen.block.Expressions.invokeStatic;
import static uk.co.strangeskies.reflection.codegen.block.Expressions.literal;
import static uk.co.strangeskies.reflection.token.ExecutableToken.staticMethods;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.codegen.block.ExpressionVisitor.ValueExpressionVisitor;
import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.TypeToken;

class TypeTokenExpression<T> implements ValueExpression<TypeToken<T>> {
	@SuppressWarnings("unchecked")
	private static final ExecutableToken<Void, TypeToken<?>> FOR_TYPE_METHOD = (ExecutableToken<Void, TypeToken<?>>) staticMethods(
			TypeToken.class).named("forType").resolveOverload(Type.class);

	@SuppressWarnings("unchecked")
	private static final ExecutableToken<Void, AnnotatedType> ANNOTATED_METHOD = (ExecutableToken<Void, AnnotatedType>) staticMethods(
			AnnotatedTypes.class).named("annotated").resolveOverload(Type.class, Annotation[].class);

	private final ValueExpression<? extends TypeToken<?>> expression;

	@SuppressWarnings("unchecked")
	public TypeTokenExpression(TypeToken<T> token) {
		AnnotatedType type = token.getAnnotatedDeclaration();
		ValueExpression<AnnotatedType> annotatedType = (ValueExpression<AnnotatedType>) getAnnotatedTypeExpression(
				type);
		expression = invokeStatic(FOR_TYPE_METHOD, annotatedType);
	}

	private ValueExpression<?> getAnnotatedTypeExpression(AnnotatedType annotatedType) {
		Type type = annotatedType.getType();
		if (type instanceof Class<?>) {
			List<ValueExpression<?>> arguments = new ArrayList<>();
			arguments.add(literal((Class<?>) type));
			arguments.addAll(getAnnotationExpressions(annotatedType.getAnnotations()));
			return invokeStatic(ANNOTATED_METHOD, arguments);
		}
		throw new IllegalArgumentException();
	}

	private List<ValueExpression<Annotation>> getAnnotationExpressions(Annotation[] annotations) {
		return stream(annotations).map(this::getAnnotationExpression).collect(toList());
	}

	private ValueExpression<Annotation> getAnnotationExpression(Annotation annotations) {

	}

	@Override
	public void accept(ValueExpressionVisitor<TypeToken<T>> visitor) {
		expression.accept(visitor);
	}
}

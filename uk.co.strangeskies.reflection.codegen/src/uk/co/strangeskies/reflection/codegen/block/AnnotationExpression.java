package uk.co.strangeskies.reflection.codegen.block;

import static uk.co.strangeskies.reflection.Annotations.getModifiedProperties;
import static uk.co.strangeskies.reflection.codegen.block.Expressions.invokeStatic;
import static uk.co.strangeskies.reflection.codegen.block.Expressions.literal;
import static uk.co.strangeskies.reflection.codegen.block.Expressions.tryLiteral;
import static uk.co.strangeskies.reflection.token.ExecutableToken.staticMethods;
import static uk.co.strangeskies.reflection.token.MethodMatcher.allMethods;
import static uk.co.strangeskies.reflection.token.OverloadResolver.resolveOverload;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.reflection.AnnotationProperty;
import uk.co.strangeskies.reflection.Annotations;
import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.TypeToken;

public class AnnotationExpression implements ValueExpression<Annotation> {
	private static final ExecutableToken<Void, AnnotationProperty> ANNOTATION_PROPERTY_CONSTRUCTOR = new TypeToken<AnnotationProperty>() {}
			.constructors()
			.collect(resolveOverload(String.class, Object.class));

	@SuppressWarnings("unchecked")
	private static final ExecutableToken<Void, Annotation> ANNOTATION_FROM_METHOD = (ExecutableToken<Void, Annotation>) staticMethods(
			Annotations.class)
					.filter(allMethods().named("from"))
					.collect(resolveOverload(Class.class, AnnotationProperty[].class))
					.asVariableArityInvocation();

	private final ValueExpression<Annotation> expression;

	public AnnotationExpression(Annotation annotation) {
		List<ValueExpression<?>> arguments = new ArrayList<>();

		arguments.add(literal(annotation.annotationType()));

		getModifiedProperties(annotation).forEach(property -> {
			ValueExpression<?> value;
			if (property.value() instanceof Annotation) {
				value = new AnnotationExpression((Annotation) property.value());
			} else {
				value = tryLiteral(property.value());
			}

			arguments.add(invokeStatic(ANNOTATION_PROPERTY_CONSTRUCTOR, literal(property.name()), value));
		});

		expression = invokeStatic(ANNOTATION_FROM_METHOD, arguments);
	}

	@Override
	public void evaluate(Scope scope) {
		expression.evaluate(scope);
	}
}

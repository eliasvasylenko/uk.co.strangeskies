/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
 * This file is part of uk.co.strangeskies.reflection.codegen.
 *
 * uk.co.strangeskies.reflection.codegen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection.codegen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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

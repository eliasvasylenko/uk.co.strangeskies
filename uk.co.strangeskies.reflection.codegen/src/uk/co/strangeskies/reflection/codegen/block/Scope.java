package uk.co.strangeskies.reflection.codegen.block;

import java.lang.reflect.Method;

import uk.co.strangeskies.reflection.token.MethodMatcher;

public interface Scope {
	InstructionVisitor instructions();

	Method resolve(MethodMatcher<?, ?> matcher);
}

package uk.co.strangeskies.reflection;

import java.lang.reflect.Member;

public interface MemberDefinition<C, T> extends Member {
	@Override
	default Class<?> getDeclaringClass() {
		throw new UnsupportedOperationException();
	}

	ClassDefinition<C> getDeclaringClassDefinition();
}

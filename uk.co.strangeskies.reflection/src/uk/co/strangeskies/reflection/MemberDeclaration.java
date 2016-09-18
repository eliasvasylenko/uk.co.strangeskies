package uk.co.strangeskies.reflection;

public interface MemberDeclaration<C, T> {
	String getName();

	ClassDefinition<C> getClassDefinition();

	MemberDefinition<C, T> define();
}

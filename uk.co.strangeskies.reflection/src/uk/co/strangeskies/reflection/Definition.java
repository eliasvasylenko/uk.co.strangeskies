package uk.co.strangeskies.reflection;

public interface Definition {
	void accept(DefinitionVisitor visitor);
}

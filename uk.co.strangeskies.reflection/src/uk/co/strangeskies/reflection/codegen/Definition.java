package uk.co.strangeskies.reflection.codegen;

public class Definition<T extends Declaration<?>> {
	private final T declaration;

	public Definition(T declaration) {
		this.declaration = declaration;
	}

	public T getDeclaration() {
		return declaration;
	}
}

package uk.co.strangeskies.reflection.codegen;

public class AnnotatedDeclaration<S extends AnnotatedSignature<?>> implements Declaration<S> {
	protected final S signature;

	public AnnotatedDeclaration(S signature) {
		this.signature = signature;
	}

	@Override
	public S getSignature() {
		return signature;
	}
}

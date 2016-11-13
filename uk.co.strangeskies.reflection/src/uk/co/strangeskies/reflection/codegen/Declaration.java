package uk.co.strangeskies.reflection.codegen;

public interface Declaration<S extends Signature<?>> {
	S getSignature();
}

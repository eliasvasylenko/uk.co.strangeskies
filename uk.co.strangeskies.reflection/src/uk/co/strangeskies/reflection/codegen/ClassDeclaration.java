package uk.co.strangeskies.reflection.codegen;

import java.util.function.Function;
import java.util.stream.Stream;

public class ClassDeclaration<E, T> implements Declaration<ClassSignature<T>> {
	private final ClassDeclaration<?, E> enclosingClass;
	private final ClassSignature<T> signature;

	public ClassDeclaration(ClassDeclaration<?, E> enclosingClass, ClassSignature<T> signature) {
		this.enclosingClass = enclosingClass;
		this.signature = signature;
	}

	public ClassDeclaration<?, E> getEnclosingClass() {
		return enclosingClass;
	}

	@Override
	public ClassSignature<T> getSignature() {
		return signature;
	}

	public Stream<? extends MethodDeclaration<T, ?>> methodDeclarations() {

	}

	public Stream<? extends MethodDeclaration<Void, ?>> staticMethodDeclarations() {

	}

	public ClassDefinition<? extends Function<String, String>> define() {
		// TODO Auto-generated method stub
		return null;
	}
}

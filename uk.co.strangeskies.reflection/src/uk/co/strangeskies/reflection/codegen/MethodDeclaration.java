package uk.co.strangeskies.reflection.codegen;

public class MethodDeclaration<C, T> extends ParameterizedDeclaration<MethodSignature<T>>
		implements MemberDeclaration<MethodSignature<T>> {
	protected MethodDeclaration(ClassDeclaration<C> classDeclaration, MethodSignature<T> signature) {
		super(signature);
	}
}

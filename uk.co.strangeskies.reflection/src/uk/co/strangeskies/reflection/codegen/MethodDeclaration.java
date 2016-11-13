package uk.co.strangeskies.reflection.codegen;

import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.reflection.token.TypeToken.overAnnotatedType;

import java.lang.reflect.AnnotatedType;
import java.util.List;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.TypeToken;

public class MethodDeclaration<C, T> extends ParameterizedDeclaration<ExecutableSignature<?>> {
	public static <C, T> MethodDeclaration<C, T> declareConstructor(
			ClassDeclaration<C, T> classDeclaration,
			ConstructorSignature signature) {
		return new MethodDeclaration<>(
				classDeclaration.getSignature().getTypeName(),
				classDeclaration.asToken().getAnnotatedDeclaration(),
				classDeclaration.getEnclosingClass(),
				signature);
	}

	public static <C, T> MethodDeclaration<C, T> declareStaticMethod(
			ClassDeclaration<C, ?> classDeclaration,
			MethodSignature<T> signature) {
		return new MethodDeclaration<>(
				signature.getName(),
				signature.getReturnType(),
				classDeclaration.getEnclosingClass(),
				signature);
	}

	public static <C, T> MethodDeclaration<C, T> declareMethod(
			ClassDeclaration<?, C> classDeclaration,
			MethodSignature<T> signature) {
		return new MethodDeclaration<>(signature.getName(), signature.getReturnType(), classDeclaration, signature);
	}

	private final String name;

	private final ClassDeclaration<?, C> owningDeclaration;
	private final TypeToken<T> returnType;
	private final List<LocalVariableExpression<?>> parameters;

	private final ErasedMethodSignature erasedSignature;

	@SuppressWarnings("unchecked")
	protected MethodDeclaration(
			String name,
			AnnotatedType returnType,
			ClassDeclaration<?, C> owningDeclaration,
			ExecutableSignature<?> signature) {
		super(signature);

		this.name = name;
		this.owningDeclaration = owningDeclaration;
		this.returnType = (TypeToken<T>) overAnnotatedType(substituteTypeVariableSignatures(returnType));
		this.parameters = signature
				.getParameters()
				.map(parameter -> createParameter((VariableSignature<?>) parameter))
				.collect(toList());

		this.erasedSignature = new ErasedMethodSignature(
				getName(),
				parameters.stream().map(v -> v.getType().getRawType()).toArray(Class<?>[]::new));
	}

	public <U> LocalVariableExpression<U> getParameter(VariableSignature<U> parameterSignature) {
		return null;
	}

	private <U> LocalVariableExpression<U> createParameter(VariableSignature<U> parameterSignature) {
		TypeToken<?> typeToken = overAnnotatedType(substituteTypeVariableSignatures(parameterSignature.getType()));

		@SuppressWarnings("unchecked")
		LocalVariableExpression<U> variable = new LocalVariableExpression<>((TypeToken<U>) typeToken);

		return variable;
	}

	public ErasedMethodSignature getErasedSignature() {
		return erasedSignature;
	}

	public ExecutableToken<C, T> asToken() {
		return null;
	}

	public String getName() {
		return name;
	}

	public TypeToken<T> getReturnType() {
		return returnType;
	}

	public Stream<LocalVariableExpression<?>> getParameters() {
		return parameters.stream();
	}
}

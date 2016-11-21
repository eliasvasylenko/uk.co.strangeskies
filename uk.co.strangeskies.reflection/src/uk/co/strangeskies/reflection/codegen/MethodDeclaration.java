package uk.co.strangeskies.reflection.codegen;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.reflection.token.TypeToken.overAnnotatedType;

import java.lang.reflect.AnnotatedType;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.TypeToken;

public class MethodDeclaration<C, T> extends ParameterizedDeclaration<ExecutableSignature<?>> {
	enum Kind {
		CONSTRUCTOR, INSTANCE_METHOD, STATIC_METHOD
	}

	private final String name;
	private final Kind kind;

	private final ClassDeclaration<?, ?> declaringClass;
	private final ClassDeclaration<?, C> owningDeclaration;
	private final TypeToken<T> returnType;
	private final List<LocalVariableExpression<?>> parameters;

	private final ErasedMethodSignature erasedSignature;

	@SuppressWarnings("unchecked")
	protected MethodDeclaration(
			String name,
			Kind kind,
			ClassDeclaration<?, ?> declaringClass,
			ClassDeclaration<?, C> owningDeclaration,
			AnnotatedType returnType,
			ExecutableSignature<?> signature) {
		super(signature);

		this.name = name;
		this.kind = kind;
		this.declaringClass = declaringClass;
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

	protected static <C, T> MethodDeclaration<C, T> declareConstructor(
			ClassDeclaration<C, T> classDeclaration,
			ConstructorSignature signature) {
		return new MethodDeclaration<>(
				classDeclaration.getSignature().getTypeName(),
				Kind.CONSTRUCTOR,
				classDeclaration,
				classDeclaration.getEnclosingClass(),
				classDeclaration.asToken().getAnnotatedDeclaration(),
				signature);
	}

	protected static <C, T> MethodDeclaration<C, T> declareStaticMethod(
			ClassDeclaration<C, ?> classDeclaration,
			MethodSignature<T> signature) {
		return new MethodDeclaration<>(
				signature.getName(),
				Kind.STATIC_METHOD,
				classDeclaration,
				classDeclaration.getEnclosingClass(),
				signature.getReturnType(),
				signature);
	}

	protected static <C, T> MethodDeclaration<C, T> declareMethod(
			ClassDeclaration<?, C> classDeclaration,
			MethodSignature<T> signature) {
		return new MethodDeclaration<>(
				signature.getName(),
				Kind.INSTANCE_METHOD,
				classDeclaration,
				classDeclaration,
				signature.getReturnType(),
				signature);
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

	public Kind getKind() {
		return kind;
	}

	public TypeToken<T> getReturnType() {
		return returnType;
	}

	public Stream<LocalVariableExpression<?>> getParameters() {
		return parameters.stream();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		/*-
		if (isPrivate())
			builder.append("private ");
		else if (isProtected())
			builder.append("protected ");
		else if (isPublic())
			builder.append("public ");
		
		if (isNative())
			builder.append("native ");
		if (isStatic())
			builder.append("static ");
		if (isStrict())
			builder.append("strictfp ");
		if (isSynchronized())
			builder.append("synchronized ");
		
		if (isAbstract())
			builder.append("abstract ");
		else if (isFinal())
			builder.append("final ");
		 */

		if (isParameterized()) {
			builder.append("<").append(getTypeVariables().map(Objects::toString).collect(joining(", "))).append("> ");
		}

		builder.append(returnType).toString();
		if (getKind() != Kind.CONSTRUCTOR)
			builder.append(" ").append(declaringClass).append(".").append(name);

		return builder
				.append("(")
				.append(signature.getParameters().map(Objects::toString).collect(joining(", ")))
				.append(")")
				.toString();
	}
}

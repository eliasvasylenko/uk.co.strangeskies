package uk.co.strangeskies.reflection.codegen;

import static uk.co.strangeskies.reflection.AnnotatedTypes.over;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Collection;

import uk.co.strangeskies.reflection.token.ExecutableParameter;
import uk.co.strangeskies.reflection.token.TypeToken;

public class VariableSignature<T> extends AnnotatedSignature<VariableSignature<T>> {
	public static VariableSignature<?> variableSignature(String variableName, AnnotatedType type) {
		return new VariableSignature<>(variableName, type);
	}

	public static VariableSignature<?> variableSignature(String variableName, Type type) {
		return new VariableSignature<>(variableName, over(type));
	}

	public static <U> VariableSignature<U> variableSignature(String variableName, Class<U> type) {
		return new VariableSignature<>(variableName, over(type));
	}

	public static <U> VariableSignature<U> variableSignature(String variableName, TypeToken<U> type) {
		return new VariableSignature<>(variableName, type.getAnnotatedDeclaration());
	}

	public static <U> VariableSignature<U> variableSignature(ExecutableParameter parameter) {
		return new VariableSignature<>(parameter.getName(), over(parameter.getType()));
	}

	private final String variableName;
	private final AnnotatedType type;

	protected VariableSignature(String variableName, AnnotatedType type) {
		this.variableName = variableName;
		this.type = type;
	}

	protected VariableSignature(String variableName, AnnotatedType type, Collection<? extends Annotation> annotations) {
		super(annotations);
		this.variableName = variableName;
		this.type = type;
	}

	@Override
	protected VariableSignature<T> withAnnotatedDeclarationData(Collection<? extends Annotation> annotations) {
		return new VariableSignature<>(variableName, type, annotations);
	}

	public AnnotatedType getType() {
		return type;
	}
}

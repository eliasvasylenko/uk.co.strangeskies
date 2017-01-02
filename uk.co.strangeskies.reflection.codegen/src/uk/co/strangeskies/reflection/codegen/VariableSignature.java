package uk.co.strangeskies.reflection.codegen;

import static java.lang.System.identityHashCode;
import static java.util.Collections.emptySet;
import static uk.co.strangeskies.reflection.AnnotatedTypes.annotated;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.token.ExecutableParameter;
import uk.co.strangeskies.reflection.token.TypeToken;

public class VariableSignature<T> implements AnnotatedSignature<VariableSignature<T>> {
	public static VariableSignature<?> variableSignature(String variableName, AnnotatedType type) {
		return new VariableSignature<>(variableName, type);
	}

	public static VariableSignature<?> variableSignature(String variableName, Type type) {
		return new VariableSignature<>(variableName, annotated(type));
	}

	public static <U> VariableSignature<U> variableSignature(String variableName, Class<U> type) {
		return new VariableSignature<>(variableName, annotated(type));
	}

	public static <U> VariableSignature<U> variableSignature(String variableName, TypeToken<U> type) {
		return new VariableSignature<>(variableName, type.getAnnotatedDeclaration());
	}

	public static <U> VariableSignature<U> variableSignature(ExecutableParameter parameter) {
		return new VariableSignature<>(parameter.getName(), annotated(parameter.getType()));
	}

	private final String variableName;
	private final AnnotatedType type;
	private final Set<Annotation> annotations;

	protected VariableSignature(String variableName, AnnotatedType type) {
		this.variableName = variableName;
		this.type = type;
		this.annotations = emptySet();
	}

	protected VariableSignature(String variableName, AnnotatedType type, Set<Annotation> annotations) {
		this.variableName = variableName;
		this.type = type;
		this.annotations = annotations;
	}

	@Override
	public Stream<? extends Annotation> getAnnotations() {
		return annotations.stream();
	}

	@Override
	public VariableSignature<T> withAnnotations(Collection<? extends Annotation> annotations) {
		return new VariableSignature<>(variableName, type, new HashSet<>(annotations));
	}

	public String getVariableName() {
		return variableName;
	}

	public AnnotatedType getType() {
		return type;
	}

	@Override
	public String toString() {
		return getType() + " " + getVariableName();
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}

	@Override
	public int hashCode() {
		return identityHashCode(this);
	}
}

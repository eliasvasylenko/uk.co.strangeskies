package uk.co.strangeskies.reflection.codegen;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.stream.Stream;

public abstract class AnnotatedDeclaration<S extends AnnotatedDeclaration<S>> {
	private final Collection<? extends Annotation> annotations;

	protected AnnotatedDeclaration() {
		annotations = emptySet();
	}

	protected AnnotatedDeclaration(Collection<? extends Annotation> annotations) {
		this.annotations = annotations;
	}

	protected AnnotatedDeclaration(AnnotatedDeclaration<?> declaration) {
		this.annotations = declaration.annotations;
	}

	public Stream<? extends Annotation> getAnnotations() {
		return annotations.stream();
	}

	public TypeVariableDeclaration withAnnotations(Annotation... annotations) {
		return withAnnotations(asList(annotations));
	}

	public TypeVariableDeclaration withAnnotations(Collection<? extends Annotation> annotations) {
		return withAnnotatedDeclarationData(annotations);
	}

	protected abstract TypeVariableDeclaration withAnnotatedDeclarationData(Collection<? extends Annotation> annotations);
}

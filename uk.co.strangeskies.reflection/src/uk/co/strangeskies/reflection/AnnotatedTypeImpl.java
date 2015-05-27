package uk.co.strangeskies.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Collection;

class AnnotatedTypeImpl implements AnnotatedType {
	private final Type type;
	private final Annotation[] annotations;

	public AnnotatedTypeImpl(Type type) {
		this.type = type;
		annotations = new Annotation[] {};
	}

	public AnnotatedTypeImpl(Type type,
			Collection<? extends Annotation> annotations) {
		this.type = type;
		this.annotations = annotations.toArray(new Annotation[annotations.size()]);
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return null;
	}

	@Override
	public Annotation[] getAnnotations() {
		return annotations;
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return annotations;
	}

	@Override
	public Type getType() {
		return type;
	}
}

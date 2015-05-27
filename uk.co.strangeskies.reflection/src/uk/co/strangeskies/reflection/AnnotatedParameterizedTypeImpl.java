package uk.co.strangeskies.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Collection;

class AnnotatedParameterizedTypeImpl extends AnnotatedTypeImpl implements
		AnnotatedParameterizedType {
	private AnnotatedType[] annotatedTypeArguments;

	public AnnotatedParameterizedTypeImpl(Type type) {
		super(type);
	}

	public AnnotatedParameterizedTypeImpl(Type type,
			Collection<? extends Annotation> annotations) {
		super(type, annotations);
	}

	@Override
	public AnnotatedType[] getAnnotatedActualTypeArguments() {
		return annotatedTypeArguments;
	}
}

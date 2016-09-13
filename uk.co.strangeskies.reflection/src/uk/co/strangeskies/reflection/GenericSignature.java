package uk.co.strangeskies.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GenericSignature {
	private final List<TypeVariableSignature> typeVariables;

	public GenericSignature() {
		this.typeVariables = new ArrayList<>();
	}

	public TypeVariableSignature addTypeVariable() {
		TypeVariableSignature typeVariable = new TypeVariableSignature();
		typeVariables.add(typeVariable);
		return typeVariable;
	}

	public GenericSignature withTypeVariable(Type... bounds) {
		return withTypeVariable(Arrays.stream(bounds).map(TypeToken::over).collect(Collectors.toList()));
	}

	public GenericSignature withTypeVariable(TypeToken<?>... bounds) {
		return withTypeVariable(Arrays.asList(bounds));
	}

	public GenericSignature withTypeVariable(List<TypeToken<?>> bounds) {
		addTypeVariable().withUpperBounds(bounds);
		return this;
	}

	public <T extends GenericDeclaration> List<TypeVariable<T>> getTypeVariables(T declaration) {
		return typeVariables.stream().<TypeVariable<T>> map(s -> new TypeVariable<T>() {
			@Override
			public <U extends Annotation> U getAnnotation(Class<U> annotationClass) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Annotation[] getAnnotations() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Annotation[] getDeclaredAnnotations() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Type[] getBounds() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public T getGenericDeclaration() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public AnnotatedType[] getAnnotatedBounds() {
				// TODO Auto-generated method stub
				return null;
			}
		}).collect(Collectors.toList());
	}
}

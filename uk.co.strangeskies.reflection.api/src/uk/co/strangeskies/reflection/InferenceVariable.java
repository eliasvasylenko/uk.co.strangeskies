package uk.co.strangeskies.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.Nullable;

public class InferenceVariable<T extends GenericDeclaration> implements
		TypeVariable<T> {
	private final TypeVariable<? extends T> typeVariable;
	private Type[] bounds;
	private final Resolver resolver;

	private InferenceVariable(Resolver resolver,
			TypeVariable<? extends T> typeVariable) {
		this.resolver = resolver;
		this.typeVariable = typeVariable;
	}

	public TypeVariable<? extends T> getTypeVariable() {
		return typeVariable;
	}

	@Override
	public <U extends Annotation> @Nullable U getAnnotation(Class<U> paramClass) {
		return typeVariable.getAnnotation(paramClass);
	}

	@Override
	public Annotation[] getAnnotations() {
		return typeVariable.getAnnotations();
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return typeVariable.getDeclaredAnnotations();
	}

	@Override
	public Type[] getBounds() {
		return bounds;
	}

	public Resolver getResolver() {
		return resolver;
	}

	@Override
	public T getGenericDeclaration() {
		return typeVariable.getGenericDeclaration();
	}

	@Override
	public String getName() {
		return "CAP#" + typeVariable.getName();
	}

	@Override
	public AnnotatedType[] getAnnotatedBounds() {
		return typeVariable.getAnnotatedBounds();
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object == null || !(object instanceof InferenceVariable))
			return false;

		InferenceVariable<?> that = (InferenceVariable<?>) object;

		return this.typeVariable.equals(that.typeVariable)
				&& this.resolver == that.resolver;
	}

	@Override
	public int hashCode() {
		return typeVariable.hashCode();
	}

	@SuppressWarnings("unchecked")
	public static <T extends GenericDeclaration> List<InferenceVariable<T>> overGenericTypeContext(
			Resolver resolver, T declaration) {
		if (resolver == null)
			throw new IllegalArgumentException(new NullPointerException());

		List<InferenceVariable<T>> inferenceVariables = new ArrayList<>();
		do {
			inferenceVariables
					.addAll(Arrays
							.stream(
									(TypeVariable<? extends T>[]) declaration.getTypeParameters())
							.map(t -> new InferenceVariable<T>(resolver, t))
							.collect(Collectors.toList()));
		} while (declaration instanceof Class
				&& (declaration = (T) ((Class<?>) declaration).getEnclosingClass()) != null);

		TypeSubstitution substitution = new TypeSubstitution();
		for (InferenceVariable<T> variable : inferenceVariables)
			substitution = substitution.where(variable.typeVariable, variable);

		for (InferenceVariable<T> variable : inferenceVariables)
			variable.bounds = Arrays.stream(variable.typeVariable.getBounds())
					.map(substitution::resolve).collect(Collectors.toList())
					.toArray(new Type[variable.typeVariable.getBounds().length]);

		return inferenceVariables;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Set<InferenceVariable> getAllMentionedBy(Type type) {
		return (Set) Types.getAllMentionedBy(type,
				InferenceVariable.class::isInstance);
	}

	public static boolean isProperType(Type type) {
		return getAllMentionedBy(type).isEmpty();
	}
}

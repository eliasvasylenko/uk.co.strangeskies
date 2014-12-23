package uk.co.strangeskies.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.checkerframework.checker.javari.qual.ReadOnly;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.google.common.reflect.TypeResolver;

public class InferenceVariable<T extends GenericDeclaration> implements
		TypeVariable<T> {
	private TypeVariable<? extends T> typeVariable;
	private Type[] bounds;

	private InferenceVariable(TypeVariable<? extends T> typeVariable) {
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

	@Override
	public T getGenericDeclaration() {
		return typeVariable.getGenericDeclaration();
	}

	@Override
	public String getName() {
		return typeVariable.getName() + "v";
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
	public boolean equals(@Nullable @ReadOnly Object paramObject) {
		return super.equals(paramObject);
	}

	@Override
	public int hashCode() {
		return typeVariable.hashCode();
	}

	@SuppressWarnings("unchecked")
	public static <T extends GenericDeclaration> List<InferenceVariable<T>> overGenericDeclaration(
			T declaration) {
		return overTypeVariables((TypeVariable<? extends T>[]) declaration
				.getTypeParameters());
	}

	@SafeVarargs
	public static <T extends GenericDeclaration> List<InferenceVariable<T>> overTypeVariables(
			TypeVariable<? extends T>... typeVariables) {
		return overTypeVariables(Arrays.asList(typeVariables));
	}

	public static <T extends GenericDeclaration> List<InferenceVariable<T>> overTypeVariables(
			Collection<? extends TypeVariable<? extends T>> typeVariables) {
		List<InferenceVariable<T>> inferenceVariables = typeVariables.stream()
				.map(t -> new InferenceVariable<T>(t)).collect(Collectors.toList());

		TypeResolver resolver = new TypeResolver();
		for (InferenceVariable<T> variable : inferenceVariables)
			resolver = resolver.where(variable.typeVariable, variable);

		for (InferenceVariable<T> variable : inferenceVariables)
			variable.bounds = Arrays.stream(variable.typeVariable.getBounds())
					.map(resolver::resolveType).collect(Collectors.toList())
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

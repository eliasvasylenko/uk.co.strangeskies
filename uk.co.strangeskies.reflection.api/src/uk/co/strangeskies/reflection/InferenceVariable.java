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

import com.google.common.reflect.TypeResolver;

public class InferenceVariable<T extends GenericDeclaration> implements
		TypeVariable<T> {
	private final TypeVariable<? extends T> typeVariable;
	private Type[] bounds;
	private final GenericTypeContext<?> genericTypeContext;

	private InferenceVariable(TypeVariable<? extends T> typeVariable,
			GenericTypeContext<?> genericTypeContext) {
		this.typeVariable = typeVariable;
		this.genericTypeContext = genericTypeContext;
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

	public GenericTypeContext<?> getGenericTypeContext() {
		return genericTypeContext;
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

		System.out.println(this
				+ " == "
				+ that
				+ " = "
				+ (typeVariable.equals(that.typeVariable) && genericTypeContext
						.equals(that.genericTypeContext)));

		return typeVariable.equals(that.typeVariable)
				&& genericTypeContext.equals(that.genericTypeContext);
	}

	@Override
	public int hashCode() {
		return typeVariable.hashCode();
	}

	public static <T extends GenericDeclaration> List<InferenceVariable<T>> overGenericTypeContext(
			GenericTypeContext<T> context) {
		return overGenericTypeContext(context, context.getGenericDeclaration());
	}

	@SuppressWarnings("unchecked")
	public static <T extends GenericDeclaration> List<InferenceVariable<T>> overGenericTypeContext(
			GenericTypeContext<?> context, T declaration) {
		if (context == null)
			throw new RuntimeException();

		List<InferenceVariable<T>> inferenceVariables = new ArrayList<>();
		do {
			inferenceVariables
					.addAll(Arrays
							.stream(
									(TypeVariable<? extends T>[]) declaration.getTypeParameters())
							.map(t -> new InferenceVariable<T>(t, context))
							.collect(Collectors.toList()));
		} while (declaration instanceof Class
				&& (declaration = (T) ((Class<?>) declaration).getEnclosingClass()) != null);

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

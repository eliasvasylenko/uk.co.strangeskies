package uk.co.strangeskies.reflection.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.checkerframework.checker.javari.qual.ReadOnly;
import org.checkerframework.checker.nullness.qual.Nullable;

import uk.co.strangeskies.reflection.RecursiveTypeVisitor;

import com.google.common.reflect.TypeResolver;

public class InferenceVariable implements TypeVariable<Executable> {
	private TypeVariable<? extends Executable> typeVariable;
	private Type[] bounds;

	private InferenceVariable(TypeVariable<? extends Executable> typeVariable) {
		this.typeVariable = typeVariable;
	}

	public TypeVariable<? extends Executable> getTypeVariable() {
		return typeVariable;
	}

	@Override
	public <T extends Annotation> @Nullable T getAnnotation(Class<T> paramClass) {
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
	public Executable getGenericDeclaration() {
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

	@SafeVarargs
	public static List<InferenceVariable> forList(
			TypeVariable<? extends Executable>... parameters) {
		return forList(Arrays.asList(parameters));
	}

	public static List<InferenceVariable> forList(
			List<TypeVariable<? extends Executable>> parameters) {
		List<InferenceVariable> inferenceVariables = parameters.stream()
				.map(InferenceVariable::new).collect(Collectors.toList());

		TypeResolver resolver = new TypeResolver();
		for (InferenceVariable variable : inferenceVariables)
			resolver = resolver.where(variable.typeVariable, variable);

		for (InferenceVariable variable : inferenceVariables)
			variable.bounds = Arrays.stream(variable.typeVariable.getBounds())
					.map(resolver::resolveType).collect(Collectors.toList())
					.toArray(new Type[variable.typeVariable.getBounds().length]);

		return inferenceVariables;
	}

	public static Set<InferenceVariable> getAllMentionedBy(Type type) {
		Set<InferenceVariable> inferenceVariables = new HashSet<>();

		RecursiveTypeVisitor.build().visitEnclosingTypes().visitParameters()
				.visitBounds().typeVariableVisitor(t -> {
					if (t instanceof InferenceVariable)
						inferenceVariables.add((InferenceVariable) t);
				}).create().visit(type);

		return inferenceVariables;
	}

	public static boolean isProperType(Type type) {
		return getAllMentionedBy(type).isEmpty();
	}
}

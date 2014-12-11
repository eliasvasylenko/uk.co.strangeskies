package uk.co.strangeskies.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.checkerframework.checker.javari.qual.ReadOnly;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.google.common.reflect.TypeResolver;

public class InferenceVariable implements TypeVariable<Executable> {
	private TypeVariable<? extends Executable> typeVariable;

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
		return typeVariable.getBounds();
	}

	@Override
	public Executable getGenericDeclaration() {
		return typeVariable.getGenericDeclaration();
	}

	@Override
	public String getName() {
		return typeVariable.getName() + "i";
	}

	@Override
	public AnnotatedType[] getAnnotatedBounds() {
		return typeVariable.getAnnotatedBounds();
	}

	@Override
	public String toString() {
		return typeVariable.toString();
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

	@SuppressWarnings("unchecked")
	public static List<InferenceVariable> forList(
			List<TypeVariable<? extends Executable>> parameters) {
		Map<Type, InferenceVariable> inferenceVariables = parameters.stream()
				.collect(Collectors.toMap(Function.identity(), InferenceVariable::new));

		for (InferenceVariable variable : inferenceVariables.values()) {
			TypeResolver resolver = new TypeResolver();

			for (Type parameter : inferenceVariables.keySet())
				if (parameter != variable.typeVariable)
					resolver = resolver.where(parameter,
							inferenceVariables.get(parameter));

			variable.typeVariable = (TypeVariable<? extends Executable>) resolver
					.resolveType(variable.typeVariable);
		}

		return new ArrayList<>(inferenceVariables.values());
	}

	public static Set<InferenceVariable> getAllMentionedBy(Type type) {
		return null;
	}
}

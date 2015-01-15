package uk.co.strangeskies.reflection;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.rmi.server.UID;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class InferenceVariable implements Type {
	private final String name;
	private Type[] bounds;
	private final Resolver resolver;

	private InferenceVariable(Resolver resolver, String name) {
		this.name = name;
		this.resolver = resolver;
	}

	private InferenceVariable(String name) {
		this.name = name;
		this.resolver = null;
		this.bounds = new Type[0];
	}

	public Type[] getBounds() {
		return bounds;
	}

	public Resolver getResolver() {
		return resolver;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}

	public static InferenceVariable capture(WildcardType wildcardType) {
		return new InferenceVariable("CAP#" + new UID());
	}

	public static Map<TypeVariable<?>, InferenceVariable> capture(
			Resolver resolver, GenericDeclaration declaration) {
		if (resolver == null)
			throw new IllegalArgumentException(new NullPointerException());

		Map<TypeVariable<?>, InferenceVariable> inferenceVariables = new HashMap<>();
		do {
			Arrays.stream(declaration.getTypeParameters()).forEach(
					t -> inferenceVariables.put(t,
							new InferenceVariable(resolver, t.getName() + "#" + new UID())));
		} while (declaration instanceof Class
				&& (declaration = ((Class<?>) declaration).getEnclosingClass()) != null);

		TypeSubstitution substitution = new TypeSubstitution();
		for (Map.Entry<TypeVariable<?>, InferenceVariable> variable : inferenceVariables
				.entrySet())
			substitution = substitution.where(variable.getKey(), variable.getValue());

		for (Map.Entry<TypeVariable<?>, InferenceVariable> variable : inferenceVariables
				.entrySet())
			variable.getValue().bounds = Arrays.stream(variable.getKey().getBounds())
					.map(substitution::resolve).collect(Collectors.toList())
					.toArray(new Type[variable.getKey().getBounds().length]);

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

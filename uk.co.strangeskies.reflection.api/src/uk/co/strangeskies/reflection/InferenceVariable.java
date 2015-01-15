package uk.co.strangeskies.reflection;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class InferenceVariable implements Type {
	private final static AtomicLong COUNTER = new AtomicLong();

	private final String name;
	final Type[] upperBounds;
	final Type[] lowerBounds;
	private final Resolver resolver;

	private InferenceVariable(Resolver resolver, String name, int lowerBounds) {
		this.name = name + COUNTER.incrementAndGet();
		this.resolver = resolver;

		upperBounds = new Type[0];
		this.lowerBounds = new Type[lowerBounds];
	}

	public InferenceVariable() {
		this(new Type[0], new Type[0]);
	}

	public InferenceVariable(Type[] upperBounds, Type[] lowerBounds) {
		this.name = "CAP#" + COUNTER.incrementAndGet();
		this.resolver = null;

		this.upperBounds = upperBounds.clone();
		this.lowerBounds = lowerBounds.clone();
	}

	public Type[] getUpperBounds() {
		return upperBounds.clone();
	}

	public Type[] getLowerBounds() {
		return lowerBounds.clone();
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

	public static Map<TypeVariable<?>, InferenceVariable> capture(
			Resolver resolver, GenericDeclaration declaration) {
		Map<TypeVariable<?>, InferenceVariable> inferenceVariables = new HashMap<>();

		List<TypeVariable<?>> declarationVariables;
		if (declaration instanceof Class)
			declarationVariables = Types.getTypeParameters((Class<?>) declaration);
		else
			declarationVariables = Arrays.asList(declaration.getTypeParameters());
		declarationVariables.forEach(t -> inferenceVariables
				.put(t,
						new InferenceVariable(resolver, t.getName() + "#",
								t.getBounds().length)));

		substituteBounds(inferenceVariables);

		return inferenceVariables;
	}

	static void substituteBounds(
			Map<TypeVariable<?>, InferenceVariable> inferenceVariables) {
		TypeSubstitution substitution = new TypeSubstitution();
		for (Map.Entry<TypeVariable<?>, InferenceVariable> variable : inferenceVariables
				.entrySet())
			substitution = substitution.where(variable.getKey(), variable.getValue());

		for (Map.Entry<TypeVariable<?>, InferenceVariable> variable : inferenceVariables
				.entrySet()) {
			for (int i = 0; i < variable.getValue().upperBounds.length; i++)
				variable.getValue().upperBounds[i] = substitution.resolve(variable
						.getKey().getBounds()[i]);
			for (int i = 0; i < variable.getValue().lowerBounds.length; i++)
				variable.getValue().lowerBounds[i] = substitution.resolve(variable
						.getKey().getBounds()[i]);
		}
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

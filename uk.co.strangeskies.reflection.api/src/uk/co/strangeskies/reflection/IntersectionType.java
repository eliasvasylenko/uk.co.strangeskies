package uk.co.strangeskies.reflection;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;

public abstract class IntersectionType implements Type {
	IntersectionType() {}

	public abstract Type[] getTypes();

	public static Type of(Type... types) {
		return of(Arrays.asList(types));
	}

	public static Type of(Collection<? extends Type> types) {
		List<Type> flattenedTypes = new ArrayList<>(types);

		if (flattenedTypes.isEmpty())
			return Object.class;
		else
			for (Type type : new ArrayList<>(flattenedTypes)) {
				if (type instanceof IntersectionType) {
					flattenedTypes.remove(type);
					flattenedTypes.addAll(Arrays.asList(((IntersectionType) type)
							.getTypes()));
				}
			}

		if (flattenedTypes.size() == 1)
			return flattenedTypes.iterator().next();

		Type rawClassType = null;
		for (Type type : new ArrayList<>(flattenedTypes)) {
			Class<?> rawType = Types.getRawType(type);
			if (!rawType.isInterface())
				if (rawClassType == null)
					rawClassType = rawType;
				else
					throw new TypeInferenceException("Illegal intersection type '"
							+ flattenedTypes
							+ "', cannot contain both of the non-interface classes '"
							+ rawClassType + "' and '" + type + "'.");
		}
		if (rawClassType != null) {
			flattenedTypes.remove(rawClassType);
			flattenedTypes.add(0, rawClassType);
		}

		try {
			Resolver resolver = new Resolver();
			resolver.incorporateTypes(Comparable.class);
			InferenceVariable<?> inferenceVariable = resolver
					.getInferenceVariable(Comparable.class.getTypeParameters()[0]);
			for (Type type : flattenedTypes) {
				resolver.incorporateConstraint(new ConstraintFormula(Kind.SUBTYPE,
						inferenceVariable, type));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new TypeInferenceException("Illegal intersection type '"
					+ flattenedTypes + "'.", e);
		}

		return uncheckedOf(flattenedTypes);
	}

	static IntersectionType uncheckedOf(Collection<? extends Type> types) {
		return new IntersectionType() {
			Type[] typeArray = types.toArray(new Type[types.size()]);

			@Override
			public Type[] getTypes() {
				return typeArray;
			}
		};
	}

	@Override
	public String toString() {
		return Arrays.stream(getTypes()).map(Types::toString)
				.collect(Collectors.joining(" & "));
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IntersectionType))
			return false;
		if (obj == this)
			return true;
		IntersectionType that = (IntersectionType) obj;
		return Arrays.equals(this.getTypes(), that.getTypes());
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(getTypes());
	}
}

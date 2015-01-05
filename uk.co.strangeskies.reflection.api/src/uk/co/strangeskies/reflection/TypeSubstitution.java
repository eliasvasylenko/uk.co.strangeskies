package uk.co.strangeskies.reflection;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TypeSubstitution {
	private final Function<Type, Type> mapping;

	public TypeSubstitution() {
		mapping = t -> null;
	}

	public TypeSubstitution(Function<Type, Type> mapping) {
		this.mapping = mapping;
	}

	public TypeSubstitution where(Type from, Type to) {
		return new TypeSubstitution(t -> Objects.equals(from, t) ? to
				: mapping.apply(t));
	}

	public Type resolve(Type type) {
		Type mapping = this.mapping.apply(type);

		if (mapping != null)
			return mapping;
		if (type == null)
			return null;
		else if (type instanceof Class)
			return type;
		else if (type instanceof TypeVariable)
			return type;
		else if (type instanceof IntersectionType)
			return IntersectionType.of(Arrays
					.stream(((IntersectionType) type).getTypes()).map(this::resolve)
					.collect(Collectors.toList()));
		else if (type instanceof WildcardType)
			if (((WildcardType) type).getLowerBounds().length > 0)
				return Types.lowerBoundedWildcard(resolve(IntersectionType
						.of(((WildcardType) type).getLowerBounds())));
			else if (((WildcardType) type).getUpperBounds().length > 0)
				return Types.upperBoundedWildcard(resolve(IntersectionType
						.of(((WildcardType) type).getUpperBounds())));
			else
				return Types.unboundedWildcard();
		else if (type instanceof GenericArrayType)
			return Types.genericArrayType(resolve(((GenericArrayType) type)
					.getGenericComponentType()));
		else if (type instanceof ParameterizedType)
			return resolve((ParameterizedType) type);
		else
			throw new IllegalArgumentException("Cannot resolve unrecognised type '"
					+ type + "' of class'" + type.getClass() + "'.");
	}

	public Type resolve(ParameterizedType type) {
		return Types.parameterizedType(resolve(type.getOwnerType()),
				Types.getRawType(type), Arrays.stream(type.getActualTypeArguments())
						.map(t -> resolve(t)).collect(Collectors.toList()));
	}
}

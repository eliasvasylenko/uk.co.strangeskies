package uk.co.strangeskies.reflection;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class IntersectionType implements Type {
	private final Type[] types;

	public IntersectionType(Type[] types) {
		this(Arrays.asList(types));
	}

	public IntersectionType(Collection<? extends Type> types) {
		List<Type> flattenedTypes = new ArrayList<>(types);
		for (int i = 0; i < flattenedTypes.size(); i++) {
			Type type = flattenedTypes.get(i);
			if (type instanceof IntersectionType)
				flattenedTypes.addAll(Arrays.asList(((IntersectionType) type)
						.getTypes()));
		}
		this.types = flattenedTypes.toArray(new Type[flattenedTypes.size()]);
	}

	public Type[] getTypes() {
		return types;
	}

	@Override
	public String toString() {
		return Arrays.stream(types).map(Objects::toString)
				.collect(Collectors.joining(" & "));
	}
}

package uk.co.strangeskies.reflection;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public interface IntersectionType extends Type {
	Type[] getTypes();

	static IntersectionType of(Type... types) {
		return of(Arrays.asList(types));
	}

	static IntersectionType of(Collection<? extends Type> types) {
		List<Type> flattenedTypes = new ArrayList<>(types);

		if (flattenedTypes.isEmpty())
			flattenedTypes.add(Object.class);
		else
			for (int i = 0; i < flattenedTypes.size(); i++) {
				Type type = flattenedTypes.get(i);
				if (type instanceof IntersectionType)
					flattenedTypes.addAll(Arrays.asList(((IntersectionType) type)
							.getTypes()));
			}

		return new IntersectionType() {
			Type[] types = flattenedTypes.toArray(new Type[flattenedTypes.size()]);

			@Override
			public Type[] getTypes() {
				return types;
			}

			@Override
			public String toString() {
				return Arrays.stream(types).map(Types::toString)
						.collect(Collectors.joining(" & "));
			}
		};
	}
}

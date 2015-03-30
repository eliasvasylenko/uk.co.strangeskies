package uk.co.strangeskies.reflection;

import java.lang.reflect.ParameterizedType;

public class ParameterizedTypeToken<T> extends TypeToken<T> {
	@SuppressWarnings("unchecked")
	public ParameterizedTypeToken(ParameterizedType type) {
		super(null, type, (Class<? super T>) type.getRawType());

		validate();
	}

	public static ParameterizedTypeToken<?> of(ParameterizedType type) {
		return new ParameterizedTypeToken<>(type);
	}
}

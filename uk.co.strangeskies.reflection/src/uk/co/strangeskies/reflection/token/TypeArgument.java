package uk.co.strangeskies.reflection.token;

import static uk.co.strangeskies.reflection.ParameterizedTypes.resolveSupertype;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public abstract class TypeArgument<T> {
	private final TypeParameter<T> parameter;
	private final TypeToken<T> type;

	public TypeArgument(TypeToken<T> type) {
		this.parameter = resolveSupertypeParameter();
		this.type = type;
	}

	public TypeArgument(Class<T> type) {
		this.parameter = resolveSupertypeParameter();
		this.type = forClass(type);
	}

	protected TypeArgument(TypeParameter<T> parameter, TypeToken<T> type) {
		this.parameter = parameter;
		this.type = type;
	}

	@SuppressWarnings("unchecked")
	private TypeParameter<T> resolveSupertypeParameter() {
		Type type = ((ParameterizedType) resolveSupertype(getClass().getGenericSuperclass(), TypeArgument.class))
				.getActualTypeArguments()[0];

		if (!(type instanceof TypeVariable<?>))
			throw new IllegalArgumentException();

		return (TypeParameter<T>) TypeParameter.forTypeVariable((TypeVariable<?>) type);
	}

	public TypeToken<T> getParameterToken() {
		return parameter;
	}

	public TypeVariable<?> getParameter() {
		return parameter.getType();
	}

	public TypeToken<T> getTypeToken() {
		return type;
	}

	public Type getType() {
		return type.getType();
	}
}

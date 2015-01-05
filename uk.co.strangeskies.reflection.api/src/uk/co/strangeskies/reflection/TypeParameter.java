package uk.co.strangeskies.reflection;

import java.lang.reflect.TypeVariable;

public class TypeParameter<T> extends TypeLiteral<T> {
	protected TypeParameter() {}

	@SuppressWarnings("unchecked")
	@Override
	public TypeVariable<? extends Class<?>> getType() {
		return (TypeVariable<? extends Class<?>>) super.getType();
	}
}

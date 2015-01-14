package uk.co.strangeskies.reflection;

import java.lang.reflect.TypeVariable;

public class TypeParameter<T> extends TypeLiteral<T> {
	protected TypeParameter() {}

	@Override
	public TypeVariable<?> getType() {
		return (TypeVariable<?>) super.getType();
	}
}

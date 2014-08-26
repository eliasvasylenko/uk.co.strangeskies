package uk.co.strangeskies.gears.mathematics.expression;

import java.lang.reflect.InvocationTargetException;

public interface CloneDecouplingExpression<T extends Cloneable> extends
		Expression<T> {
	@Override
	@SuppressWarnings("unchecked")
	public default T decoupleValue() {
		try {
			return (T) Object.class.getMethod("clone").invoke(getValue());
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new UnsupportedOperationException();
		}
	}
}

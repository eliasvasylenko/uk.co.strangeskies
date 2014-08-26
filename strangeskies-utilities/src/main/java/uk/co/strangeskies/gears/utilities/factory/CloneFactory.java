package uk.co.strangeskies.gears.utilities.factory;

import java.lang.reflect.InvocationTargetException;

public class CloneFactory<T extends Cloneable> implements Factory<T> {
	private T template;

	public CloneFactory(T template) {
		this.template = template;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T create() {
		try {
			return (T) template.getClass().getMethod("clone").invoke(template);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Template object cannot be cloned", e);
		}
	}

	public void setTemplate(T template) {
		this.template = template;
	}
}

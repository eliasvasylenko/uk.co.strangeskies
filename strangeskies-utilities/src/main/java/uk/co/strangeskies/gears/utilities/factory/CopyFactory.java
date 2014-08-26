package uk.co.strangeskies.gears.utilities.factory;

import uk.co.strangeskies.gears.utilities.Copyable;

public class CopyFactory<T extends Copyable<T>> implements Factory<T> {
	private Copyable<T> template;

	public CopyFactory(Copyable<T> template) {
		this.template = template;
	}

	@Override
	public T create() {
		return template.copy();
	}

	public void setTemplate(Copyable<T> template) {
		this.template = template;
	}
}

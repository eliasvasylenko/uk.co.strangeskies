package uk.co.strangeskies.utilities;

import org.checkerframework.checker.igj.qual.I;
import org.checkerframework.checker.igj.qual.Mutable;

@I
public class IdentityProperty<T> implements @I Property<T, @I T> {
	private @I T value;

	public IdentityProperty() {
	}

	public IdentityProperty(T value) {
		this.value = value;
	}

	@Override
	public @I T set(@Mutable IdentityProperty<T> this, @I T to) {
		value = to;
		return value;
	}

	@Override
	public @I T get() {
		return value;
	}
}

package uk.co.strangeskies.utilities;

import org.checkerframework.checker.igj.qual.Mutable;

public interface Copyable<S extends Copyable<S>> {
	public @Mutable S copy();
}

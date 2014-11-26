package uk.co.strangeskies.utilities;

import checkers.igj.quals.Mutable;

public interface Copyable<S extends Copyable<S>> {
	public @Mutable S copy();
}

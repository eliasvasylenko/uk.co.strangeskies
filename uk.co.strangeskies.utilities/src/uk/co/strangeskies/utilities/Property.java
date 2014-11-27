package uk.co.strangeskies.utilities;

import org.checkerframework.checker.igj.qual.I;
import org.checkerframework.checker.igj.qual.Mutable;

@I
public interface Property<T extends R, R> {
	public @I("to") T set(@Mutable Property<T, R> this, @I("to") R to);

	public @I T get();
}

package uk.co.strangeskies.utilities;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

public class Isomorphism {
	private final Map<?, ?> copiedNodes = new IdentityHashMap<>();

	public <S> S getCopy(Copyable<? extends S> node) {
		return getCopy(node, Copyable::copy);
	}

	@SuppressWarnings("unchecked")
	public <S, C> S getCopy(C node, Function<C, S> copyFunction) {
		return ((Map<C, S>) copiedNodes).computeIfAbsent(node, copyFunction::apply);
	}
}

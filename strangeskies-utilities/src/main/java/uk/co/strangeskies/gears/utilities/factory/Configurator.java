package uk.co.strangeskies.gears.utilities.factory;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public abstract class Configurator<T> implements Factory<T> {
	private boolean complete = false;

	private final Set<Consumer<T>> listeners;

	public Configurator() {
		listeners = new HashSet<>();
	}

	@Override
	public final T create() {
		if (complete)
			throw new StaleBuilderStateException(this);

		T created;
		try {
			created = tryCreate();
		} catch (Exception e) {
			throw new InvalidBuildStateException(this, e);
		}

		listeners.stream().forEach(c -> c.accept(created));

		complete = true;

		return created;
	}

	protected void addResultListener(Consumer<T> listener) {
		listeners.add(listener);
	}

	protected abstract T tryCreate();

	public final boolean isComplete() {
		return complete;
	}

	public final void assertNotStale() {
		if (complete)
			throw new StaleBuilderStateException(this);
	}

	public final void assertComplete() {
		if (!complete)
			throw new IncompleteBuildStateException(this);
	}
}

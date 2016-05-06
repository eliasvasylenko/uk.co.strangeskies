package uk.co.strangeskies.utilities;

public interface ObservableValue<T> extends Observable<T> {
	interface Change<T> {
		T newValue();

		T previousValue();
	}

	T get();

	Observable<Change<T>> changes();
}

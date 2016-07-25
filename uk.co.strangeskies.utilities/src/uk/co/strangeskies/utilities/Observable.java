/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Simple interface for an observable object, with methods to add and remove
 * observers expecting the applicable type of message.
 * <p>
 * Unless otherwise specified, implementations should conceptually maintain the
 * collection of observers according to the semantics of {@link Set} by object
 * equality. Implementers should note that methods for weak observers and
 * terminating observers must be reimplemented if anything other than default
 * equality is used to determine membership.
 * 
 * @author Elias N Vasylenko
 * @param <M>
 *          The message type. This may be {@link Void} if no message need be
 *          sent.
 */
public interface Observable<M> {
	/**
	 * A weak observer is one which automatically removes itself from an
	 * observable once it is otherwise available for garbage collection.
	 * 
	 * @author Elias N Vasylenko
	 * @param <M>
	 *          The message type
	 * @param <O>
	 *          The owner type
	 */
	class WeakObserver<M, O> implements Consumer<M> {
		private final Observable<? extends M> observable;
		private final Function<? super O, Consumer<? super M>> consumer;
		private final WeakReference<O> owner;

		protected WeakObserver(Observable<? extends M> observable, Function<? super O, Consumer<? super M>> consumer,
				O owner) {
			this.observable = observable;

			this.consumer = consumer;
			this.owner = new WeakReference<>(owner);
		}

		@Override
		public void accept(M t) {
			O owner = this.owner.get();
			if (owner == null) {
				new Thread(() -> observable.removeObserver(this)).start();
			} else {
				consumer.apply(owner).accept(t);
			}
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			} else if (!(obj instanceof WeakObserver)) {
				return false;
			}

			return Objects.equals(owner.get(), ((WeakObserver<?, ?>) obj).owner.get());
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(owner.get());
		}
	}

	/**
	 * A terminating observer is one which may easily conditionally remove itself
	 * from an observable upon receipt of an event.
	 * 
	 * @author Elias N Vasylenko
	 * @param <M>
	 *          The message type
	 */
	class TerminatingObserver<M> implements Consumer<M> {
		private final Observable<? extends M> observable;
		private final Function<? super M, Boolean> observer;

		protected TerminatingObserver(Observable<? extends M> observable, Function<? super M, Boolean> observer) {
			this.observable = observable;
			this.observer = observer;
		}

		@Override
		public void accept(M event) {
			if (observer.apply(event)) {
				observable.removeObserver(this);
			}
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			} else if (!(obj instanceof TerminatingObserver)) {
				return false;
			}

			return Objects.equals(observer, ((TerminatingObserver<?>) obj).observer);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(observer);
		}
	}

	/**
	 * Observers added will receive messages from this Observable. A weak observer
	 * will be removed automatically once it is otherwise available for garbage
	 * collection.
	 * 
	 * @param observer
	 *          An observer to add
	 * @return True if the observer was successfully added, false otherwise.
	 */
	default boolean addWeakObserver(Consumer<? super M> observer) {
		return addObserver(new WeakObserver<>(this, o -> o, observer));
	}

	/**
	 * Observers added will receive messages from this Observable. A weak observer
	 * will be removed automatically once the given owner is available for garbage
	 * collection.
	 * <p>
	 * Only one weak observer can be registered for any given owner.
	 * <p>
	 * The observer itself must be given in the form of a function applicable to
	 * the owner to ensure it does not create a reference to the owner and prevent
	 * garbage collection.
	 * 
	 * @param <O>
	 *          The type of the owner
	 * @param observer
	 *          An observer to add
	 * @param owner
	 *          The owner of the observer
	 * @return True if the observer was successfully added, false otherwise.
	 */
	default <O> boolean addWeakObserver(O owner, Function<? super O, Consumer<? super M>> observer) {
		return addObserver(new WeakObserver<>(this, observer, owner));
	}

	/**
	 * Remove any observer from this Observable which has the given owner.
	 * Observers added without an explicitly given owner are considered to be
	 * owned by themselves.
	 * <p>
	 * Only one weak observer can be registered for any given owner.
	 * 
	 * @param owner
	 *          An observer to remove, or an owner whose observer is to be removed
	 * @return True if the observer was successfully removed, false otherwise
	 */
	default boolean removeWeakObserver(Object owner) {
		return removeObserver(new WeakObserver<>(this, null, owner));
	}

	/**
	 * Observers added will receive messages from this Observable. Terminating
	 * observers may conditionally remove themselves from the observable upon
	 * receipt of events by returning from the observer function.
	 * 
	 * @param observer
	 *          An observer to add, a function from event type to a boolean, a
	 *          true value of which will remove the observer
	 * @return True if the observer was successfully added, false otherwise
	 */
	default boolean addTerminatingObserver(Function<? super M, Boolean> observer) {
		return addObserver(new TerminatingObserver<>(this, observer));
	}

	/**
	 * Observers removed will no longer receive messages from this Observable.
	 * 
	 * @param observer
	 *          An observer to remove
	 * @return True if the observer was successfully removed, false otherwise
	 */
	default boolean removeTerminatingObserver(Function<? super M, Boolean> observer) {
		return removeObserver(new TerminatingObserver<>(this, observer));
	}

	/**
	 * Observers added will receive messages from this Observable.
	 * 
	 * @param observer
	 *          An observer to add
	 * @return True if the observer was successfully added, false otherwise
	 */
	boolean addObserver(Consumer<? super M> observer);

	/**
	 * Observers removed will no longer receive messages from this Observable.
	 * 
	 * @param observer
	 *          An observer to remove
	 * @return True if the observer was successfully removed, false otherwise
	 */
	boolean removeObserver(Consumer<? super M> observer);

	/**
	 * Get an observable instance which never fires events. As an optimisation,
	 * attempts to add and remove observers will always succeed regardless of
	 * whether or not the observer is already added. In cases where this is a
	 * problem, consider using an instance of {@link ObservableImpl} instead.
	 * 
	 * @param <M>
	 *          the message type for the immutable observable
	 * 
	 * @return an observable instance which never fires events
	 */
	@SuppressWarnings("unchecked")
	static <M> Observable<M> immutable() {
		return (Observable<M>) ImmutableObservable.IMMUTABLE;
	}
}

interface ImmutableObservable<M> extends Observable<M> {
	static Observable<?> IMMUTABLE = new ImmutableObservable<Object>() {};

	@Override
	default boolean addObserver(Consumer<? super M> observer) {
		return true;
	}

	@Override
	default boolean removeObserver(Consumer<? super M> observer) {
		return true;
	}
}

/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.observable.
 *
 * uk.co.strangeskies.observable is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.observable is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.observable;

import java.lang.ref.WeakReference;
import java.util.Set;
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
	 * @see Observable#addOwnedObserver(Object, Observer)
	 */
	@SuppressWarnings("javadoc")
	abstract class OwnedObserver<M> implements Observer<M> {
		abstract Object getOwner();

		protected OwnedObserver() {}

		@Override
		public final boolean equals(Object obj) {
			if (this == obj) {
				return true;
			} else if (!getClass().equals(obj.getClass())) {
				return false;
			}

			return getOwner() == ((OwnedObserver<?>) obj).getOwner();
		}

		@Override
		public final int hashCode() {
			return System.identityHashCode(getOwner());
		}
	}

	/**
	 * @see Observable#addOwnedObserver(Object, Observer)
	 */
	@SuppressWarnings("javadoc")
	class OwnedObserverImpl<M> extends OwnedObserver<M> {
		private final Observer<? super M> observer;
		private final Object owner;

		protected OwnedObserverImpl(Observer<? super M> observer, Object owner) {
			this.observer = observer;
			this.owner = owner;
		}

		@Override
		Object getOwner() {
			return owner;
		}

		@Override
		public void notify(M event) {
			observer.notify(event);
		}
	}

	/**
	 * @see Observable#addWeakObserver(Observer)
	 */
	@SuppressWarnings("javadoc")
	class WeakObserver<M, O> extends OwnedObserver<M> {
		private final Observable<? extends M> observable;
		private final Function<? super O, Observer<? super M>> Observer;
		private final WeakReference<O> owner;

		protected WeakObserver(
				Observable<? extends M> observable,
				Function<? super O, Observer<? super M>> Observer,
				O owner) {
			this.observable = observable;

			this.Observer = Observer;
			this.owner = new WeakReference<>(owner);
		}

		@Override
		O getOwner() {
			return owner.get();
		}

		@Override
		public void notify(M t) {
			O owner = getOwner();
			if (owner == null) {
				observable.removeObserver(this);
			} else {
				Observer.apply(owner).notify(t);
			}
		}
	}

	/**
	 * @see Observable#addTerminatingObserver(Function)
	 */
	@SuppressWarnings("javadoc")
	class TerminatingObserver<M> extends OwnedObserver<M> {
		private final Observable<? extends M> observable;
		private final Function<? super M, Observation> observer;
		private final Object owner;

		protected TerminatingObserver(
				Observable<? extends M> observable,
				Function<? super M, Observation> observer,
				Object owner) {
			this.observable = observable;
			this.observer = observer;
			this.owner = owner;
		}

		@Override
		Object getOwner() {
			return owner;
		}

		@Override
		public void notify(M event) {
			if (observer.apply(event) == Observation.TERMINATE) {
				observable.removeObserver(this);
			}
		}
	}

	/**
	 * Control token for terminating observer behavior.
	 * 
	 * @author Elias N Vasylenko
	 */
	public static enum Observation {
		/**
		 * Terminate observation.
		 */
		TERMINATE,
		/**
		 * Continue observation.
		 */
		CONTINUE
	}

	/**
	 * Observers added will receive messages from this Observable. A weak observer
	 * will be removed automatically once it is otherwise available for garbage
	 * collection.
	 * 
	 * @param observer
	 *          an observer to add
	 * @return true if the observer was successfully added, false otherwise
	 */
	default boolean addWeakObserver(Observer<? super M> observer) {
		return addWeakObserver(observer, o -> o);
	}

	/**
	 * Observers added will receive messages from this Observable. A weak observer
	 * will be removed automatically once the given owner is available for garbage
	 * collection.
	 * <p>
	 * The observer itself must be given in the form of a function applicable to
	 * the owner to ensure it does not create a reference to the owner and prevent
	 * garbage collection.
	 * 
	 * TODO be careful not to capture the owner in a lambda as this will prevent
	 * garbage collection (give code example)
	 * 
	 * @see #addOwnedObserver(Object, Observer)
	 * 
	 * @param <O>
	 *          the type of the owner
	 * @param observer
	 *          an observer to add
	 * @param owner
	 *          the owner of the observer
	 * @return true if the observer was successfully added, false otherwise
	 */
	default <O> boolean addWeakObserver(O owner, Function<? super O, Observer<? super M>> observer) {
		return addObserver(new WeakObserver<>(this, observer, owner));
	}

	/**
	 * Observers removed will no longer receive messages from this Observable.
	 * 
	 * @param owner
	 *          the owner of the observer to remove
	 * @return true if the observer was successfully removed, false otherwise
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
	 *          an observer to add, a function from event type to a boolean, a
	 *          false value of which will remove the observer
	 * @return true if the observer was successfully added, false otherwise
	 */
	default boolean addTerminatingObserver(Function<? super M, Observation> observer) {
		return addTerminatingObserver(observer, observer);
	}

	/**
	 * Observers added will receive messages from this Observable. Terminating
	 * observers may conditionally remove themselves from the observable upon
	 * receipt of events by returning the {@link Observation#TERMINATE termination
	 * token} from the observer function.
	 * 
	 * @see #addOwnedObserver(Object, Observer)
	 * 
	 * @param <O>
	 *          the type of the owner
	 * @param observer
	 *          an observer to add
	 * @param owner
	 *          the owner of the observer
	 * @return true if the observer was successfully added, false otherwise
	 */
	default <O> boolean addTerminatingObserver(Object owner, Observer<? super M> observer) {
		return addObserver(new TerminatingObserver<>(this, m -> {
			observer.notify(m);
			return Observation.TERMINATE;
		}, owner));
	}

	/**
	 * Observers added will receive messages from this Observable. Terminating
	 * observers may conditionally remove themselves from the observable upon
	 * receipt of events by returning from the observer function.
	 * 
	 * @param observer
	 *          an observer to add, a function from event type to a boolean, a
	 *          false value of which will remove the observer
	 * @return true if the observer was successfully added, false otherwise
	 */
	default boolean addTerminatingObserver(Observer<? super M> observer) {
		return addTerminatingObserver(observer, observer);
	}

	/**
	 * Observers added will receive messages from this Observable. Terminating
	 * observers may conditionally remove themselves from the observable upon
	 * receipt of events by returning the {@link Observation#TERMINATE termination
	 * token} from the observer function.
	 * 
	 * @see #addOwnedObserver(Object, Observer)
	 * 
	 * @param <O>
	 *          the type of the owner
	 * @param observer
	 *          an observer to add
	 * @param owner
	 *          the owner of the observer
	 * @return true if the observer was successfully added, false otherwise
	 */
	default <O> boolean addTerminatingObserver(Object owner, Function<? super M, Observation> observer) {
		return addObserver(new TerminatingObserver<>(this, observer, owner));
	}

	/**
	 * Observers removed will no longer receive messages from this Observable.
	 * 
	 * @param owner
	 *          the owner of the observer to remove
	 * @return true if the observer was successfully removed, false otherwise
	 */
	default boolean removeTerminatingObserver(Object owner) {
		return removeObserver(new TerminatingObserver<>(this, null, owner));
	}

	/**
	 * This method adds an observable and marks it as "owned" by a specific object
	 * instance. Ownership implies the following properties:
	 * <p>
	 * 
	 * <ul>
	 * <li>
	 * 
	 * An observer with an owner may only be removed by reference to its owner via
	 * {@link #removeOwnedObserver(Object)}, though the owner may be the
	 * observable itself.
	 * 
	 * </li>
	 * <li>
	 * 
	 * Only one observable may be added per owner, where owners are compared by
	 * identity.
	 * 
	 * </li>
	 * </ul>
	 * <p>
	 * 
	 * This allows us, for example, to add an observer as a method reference and
	 * still be able to manually remove the observer:
	 * 
	 * <pre>
	 * {@code
	 * Observable<String> observable = ...;
	 * List<String> list = ...;
	 * 
	 * observable.addObserver(list, list:add);
	 * observable.removeObserver(list);
	 * }
	 * </pre>
	 * 
	 * Whereas the following more naive attempt may fail to remove the observer:
	 * 
	 * <pre>
	 * {@code
	 * observable.addObserver(list:add);
	 * observable.removeObserver(list:add);
	 * }
	 * </pre>
	 * 
	 * @param owner
	 *          the owner of the observer
	 * @param observer
	 *          an observer to add
	 * @return true if the observer was successfully added, false otherwise
	 */
	default boolean addOwnedObserver(Object owner, Observer<? super M> observer) {
		return addObserver(new OwnedObserverImpl<>(observer, owner));
	}

	/**
	 * Observers removed will no longer receive messages from this Observable.
	 * 
	 * @param owner
	 *          the owner of the observer to remove
	 * @return true if the observer was successfully removed, false otherwise
	 */
	default boolean removeOwnedObserver(Object owner) {
		return removeObserver(new OwnedObserverImpl<>(null, owner));
	}

	/**
	 * Observers added will receive messages from this Observable.
	 * 
	 * @param observer
	 *          an observer to add
	 * @return true if the observer was successfully added, false otherwise
	 */
	boolean addObserver(Observer<? super M> observer);

	/**
	 * Observers removed will no longer receive messages from this Observable.
	 * 
	 * @param observer
	 *          an observer to remove
	 * @return true if the observer was successfully removed, false otherwise
	 */
	boolean removeObserver(Observer<? super M> observer);

	/**
	 * Get an observable instance which never fires events. As an optimization,
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
	default boolean addObserver(Observer<? super M> observer) {
		return true;
	}

	@Override
	default boolean removeObserver(Observer<? super M> observer) {
		return true;
	}
}

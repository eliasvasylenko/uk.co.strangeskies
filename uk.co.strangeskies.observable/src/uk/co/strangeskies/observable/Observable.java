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

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;

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
   * Observers added will receive messages from this Observable.
   * 
   * @param observer
   *          an observer to add
   * @return true if the observer was successfully added, false otherwise
   */
  Observation<M> observe(Observer<? super M> observer);

  default Observation<M> observe() {
    return observe(m -> {});
  }

  default <T> Observable<T> compose(Function<Observable<M>, Observable<T>> transformation) {
    return transformation.apply(this);
  }

  public static <M> Collector<M, ?, Observable<M>> toObservable() {
    return collectingAndThen(toList(), Observable::of);
  }

  default <R, A> CompletableFuture<R> collect(Collector<? super M, A, ? extends R> collector) {
    CompletableFuture<R> future = new CompletableFuture<>();

    observe(new Observer<M>() {
      private A accumulation = collector.supplier().get();

      @Override
      public void onComplete() {
        future.complete(collector.finisher().apply(accumulation));
      }

      @Override
      public void onFail(Throwable t) {
        future.completeExceptionally(t);
      }

      @Override
      public void onNext(M message) {
        collector.accumulator().accept(accumulation, message);
      }
    });

    return future;
  }

  /**
   * Derive an observable which automatically disposes of observers at some point
   * after they are no longer weakly reachable.
   * 
   * @return the derived observable
   */
  default Observable<M> weakReference() {
    return observer -> new ReferenceObservation<>(this, observer, WeakReference::new);
  }

  /**
   * Derive an observable which automatically disposes of observers at some point
   * after the given owner is no longer weakly reachable.
   * <p>
   * Care should be taken not to refer to the owner directly in any observer
   * logic, as this will create a strong reference to the owner, preventing it
   * from becoming unreachable. For this reason, the message is transformed into
   * an {@link OwnedMessage}, which may create references to the owner on demand
   * within observer logic without retainment.
   * 
   * @param owner
   *          the referent
   * @return the derived observable
   */
  default <O> Observable<OwnedMessage<O, M>> weakReference(O owner) {
    return observer -> new ReferenceOwnedObservation<>(this, observer, owner, WeakReference::new);
  }

  /**
   * Derive an observable which automatically disposes of observers at some point
   * after they are no longer softly reachable.
   * 
   * @return the derived observable
   */
  default Observable<M> softReference() {
    return observer -> new ReferenceObservation<>(this, observer, SoftReference::new);
  }

  /**
   * Derive an observable which automatically disposes of observers at some point
   * after the given owner is no longer softly reachable.
   * <p>
   * Care should be taken not to refer to the owner directly in any observer
   * logic, as this will create a strong reference to the owner, preventing it
   * from becoming unreachable. For this reason, the message is transformed into
   * an {@link OwnedMessage}, which may create references to the owner on demand
   * within observer logic without retainment.
   * 
   * @param owner
   *          the referent
   * @return the derived observable
   */
  default <O> Observable<OwnedMessage<O, M>> softReference(O owner) {
    return observer -> new ReferenceOwnedObservation<>(this, observer, owner, SoftReference::new);
  }

  /**
   * Derive an observable which re-emits messages on the given executor.
   * 
   * @param executor
   *          the target executor
   * @return the derived observable
   */
  default Observable<M> executeOn(Executor executor) {
    return new ExecutorObservable<>(this, executor);
  }

  /**
   * Derive an observable which transforms messages according to the given
   * mapping.
   * 
   * @param mapping
   *          the mapping function
   * @return the derived observable
   */
  default <T> Observable<T> map(Function<? super M, ? extends T> mapping) {
    return observer -> new MappingObservation<>(this, observer, mapping);
  }

  /**
   * Derive an observable which passes along only those messages which match the
   * given condition.
   * 
   * @param condition
   *          the terminating condition
   * @return the derived observable
   */
  default Observable<M> filter(Predicate<? super M> condition) {
    return observer -> new FilteringObservation<>(this, observer, condition);
  }

  /**
   * Derive an observable which completes and disposes itself after receiving a
   * message which matches the given condition.
   * 
   * @param condition
   *          the terminating condition
   * @return the derived observable
   */
  default Observable<M> takeWhile(Predicate<? super M> condition) {
    return observer -> new TerminatingObservation<>(this, observer, condition);
  }

  /**
   * Derive an observable which completes and disposes itself after receiving a
   * message which matches the given condition.
   * 
   * @param condition
   *          the terminating condition
   * @return the derived observable
   */
  default Observable<M> dropWhile(Predicate<? super M> condition) {
    return observer -> new SkippingObservation<>(this, observer, condition);
  }

  /**
   * Derive an observable which completes and disposes itself after receiving a
   * message which matches the given condition.
   * 
   * @param mapping
   *          the terminating condition
   * @return the derived observable
   */
  default <T> Observable<T> flatMap(
      Function<? super M, ? extends Observable<? extends T>> mapping) {
    return new MergingObservable<>(map(mapping));
  }

  /*
   * Static factories
   */

  @SafeVarargs
  public static <M> Observable<M> of(M... messages) {
    return of(Arrays.asList(messages));
  }

  public static <M> Observable<M> of(Collection<? extends M> messages) {
    return new IterableObservable<>(messages);
  }

  @SafeVarargs
  public static <M> Observable<M> merge(Observable<? extends M>... observables) {
    return merge(Arrays.asList(observables));
  }

  public static <M> Observable<M> merge(Collection<? extends Observable<? extends M>> observables) {
    return of(observables).flatMap(identity());
  }
}

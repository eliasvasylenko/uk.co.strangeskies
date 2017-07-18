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
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.observable.Observer.onCompletion;
import static uk.co.strangeskies.observable.Observer.onObservation;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

import uk.co.strangeskies.utility.IdentityProperty;
import uk.co.strangeskies.utility.Property;

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
  Observation observe(Observer<? super M> observer);

  default Observation observe() {
    return observe(m -> {});
  }

  default <T> Observable<T> compose(Function<Observable<M>, Observable<T>> transformation) {
    return transformation.apply(this);
  }

  public static <M> Collector<M, ?, Observable<M>> toObservable() {
    return collectingAndThen(toList(), Observable::of);
  }

  default Observable<M> then(Observer<M> action) {
    return observer -> observe(new MultiPassthroughObserver<M>(observer, action));
  }

  default Observable<M> thenAfter(Observer<M> action) {
    return observer -> observe(new MultiPassthroughObserver<M>(action, observer));
  }

  default Observable<M> requestNextInitially() {
    return then(onObservation(o -> o.requestNext()));
  }

  default Observable<M> requestInitially(long count) {
    return then(onObservation(o -> o.request(count)));
  }

  default Observable<M> requestUnboundedInitially() {
    return then(onObservation(o -> o.requestUnbounded()));
  }

  /**
   * Derive an observable which automatically disposes of observers at some point
   * after they are no longer weakly reachable.
   * 
   * @return the derived observable
   */
  default Observable<M> weakReference() {
    return observer -> observe(new ReferenceObserver<>(observer, WeakReference::new));
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
    return observer -> observe(new ReferenceOwnedObserver<>(observer, owner, WeakReference::new));
  }

  /**
   * Derive an observable which automatically disposes of observers at some point
   * after they are no longer softly reachable.
   * 
   * @return the derived observable
   */
  default Observable<M> softReference() {
    return observer -> observe(new ReferenceObserver<>(observer, SoftReference::new));
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
    return observer -> observe(new ReferenceOwnedObserver<>(observer, owner, SoftReference::new));
  }

  default Observable<DisposableMessage<M>> terminating() {
    return observer -> observe(new TerminatingObserver<>(observer));
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
    return observer -> observe(new MappingObserver<>(observer, mapping));
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
    return observer -> observe(new FilteringObserver<>(observer, condition));
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
    return observer -> observe(new TakeWhileObserver<>(observer, condition));
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
    return observer -> observe(new SkipWhileObserver<>(observer, condition));
  }

  /**
   * Derive an observable which maps each message to an intermediate observable,
   * then merges the messages from the intermediate observables into a single
   * source.
   * <p>
   * An unbounded request is made to the upstream observable, so it is not
   * required to support backpressure.
   * <p>
   * The intermediate observables are not required to support backpressure, as an
   * unbounded request will be made to them and the downstream observable will
   * forward every message as soon as it is available. Because of this, The
   * downstream observable does not support backpressure.
   * 
   * @param mapping
   *          the terminating condition
   * @return the derived observable
   */
  default <T> Observable<T> flatMapMerge(
      Function<? super M, ? extends Observable<? extends T>> mapping) {
    return new MergingObservable<>(map(mapping));
  }

  /**
   * Map each message to an intermediate observable. Requests from downstream are
   * then handed to the intermediate observables, and the responding messages are
   * passed downstream.
   * <p>
   * An unbounded request is made to the upstream observable, so it is not
   * required to support backpressure.
   * <p>
   * The intermediate observables must support backpressure. Priority for
   * forwarding requests to intermediate observables is determined as follows:
   * fewest outstanding requests, then fewest total requests, then first taken
   * from upstream.
   * 
   * @param mapping
   *          the terminating condition
   * @return the derived observable
   */
  default <T> Observable<T> flatMapInterleave(
      Function<? super M, ? extends Observable<? extends T>> mapping) {
    return new MergingObservable<>(map(mapping));
  }

  /**
   * Derive an observable which sequentially maps each message to an intermediate
   * observable, then merges the messages from the intermediate observables into a
   * single source.
   * <p>
   * The upstream observable must support backpressure.
   * <p>
   * The intermediate observables must support backpressure. All requests will be
   * forwarded to the intermediate observable at the front of the queue until it
   * is complete, then subsequent requests are made to the next observable in the
   * queue and so on until the queue is empty.
   * 
   * @param mapping
   *          the terminating condition
   * @return the derived observable
   */
  default <T> Observable<T> flatMap(
      Function<? super M, ? extends Observable<? extends T>> mapping) {
    return new MergingObservable<>(map(mapping));
  }

  default <R> CompletableFuture<R> reduce(R identity, BiFunction<R, ? super M, R> accumulator) {
    CompletableFuture<R> future = new CompletableFuture<>();

    Property<Observation> observation = new IdentityProperty<>();
    observation.set(
        thenAfter(onCompletion(() -> observation.get().requestNext()))
            .reducing(() -> identity, accumulator)
            .observe(m -> future.complete(m)));

    return future;
  }

  /**
   * Introduce backpressure by reducing messages until a request is made
   * downstream, then forwarding the collection.
   * 
   * @param identity
   *          the identity value for the accumulating function
   * @param accumulator
   *          an associative, non-interfering, stateless function for combining
   *          two values
   * @return an observable over the reduced values
   */
  default <R> Observable<R> reducing(
      Supplier<R> identity,
      BiFunction<R, ? super M, R> accumulator) {
    return null; // TODO
  }

  /**
   * Introduce backpressure by reducing messages until a request is made
   * downstream, then forwarding the collection.
   * 
   * @param accumulator
   *          an associative, non-interfering, stateless function for combining
   *          two values
   * @return an observable over the reduced values
   */
  default Observable<M> reducing(BinaryOperator<M> accumulator) {
    return requestUnboundedInitially(); // .TODO
  }

  default <R, A> CompletableFuture<R> collect(Collector<? super M, A, ? extends R> collector) {
    CompletableFuture<R> future = new CompletableFuture<>();

    Property<Observation> observation = new IdentityProperty<>();
    observation.set(
        thenAfter(onCompletion(() -> observation.get().requestNext()))
            .collecting(collector)
            .observe(m -> future.complete(m)));

    return future;
  }

  /**
   * Introduce backpressure by collecting messages until a request is made
   * downstream, then forwarding the collection.
   * 
   * @param collector
   *          the collector to apply to incoming messages
   * @return an observable over the collected values
   */
  default <R, A> Observable<R> collecting(Collector<? super M, A, ? extends R> collector) {
    return reducing(collector.supplier(), (a, m) -> {
      collector.accumulator();
      return a;
    }).map(collector.finisher());
  }

  default Observable<List<M>> aggregating() {
    return aggregating(256);
  }

  default Observable<List<M>> aggregating(long toCapacity) {
    return collecting(toCollection(() -> new MaximumCapacityList<M>(toCapacity)));
  }

  default Observable<M> latest() {
    return reducing((a, b) -> a);
  }

  default Observable<M> earliest() {
    return reducing((a, b) -> b);
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
    return of(observables).flatMapMerge(identity());
  }
}

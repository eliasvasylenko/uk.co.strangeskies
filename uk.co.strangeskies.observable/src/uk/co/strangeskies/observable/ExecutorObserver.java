package uk.co.strangeskies.observable;

import java.util.concurrent.Executor;

public class ExecutorObserver<T> extends PassthroughObserver<T, T> {
	private final Executor executor;

	public ExecutorObserver(Observer<? super T> downstreamObserver, Executor executor) {
		super(downstreamObserver);
		this.executor = executor;
	}

	@Override
	public void onNext(T message) {
		executor.execute(() -> getDownstreamObserver().onNext(message));
	}

	@Override
	public void onObserve(Observation observation) {
		executor.execute(() -> getDownstreamObserver().onObserve(observation));
	}

	@Override
	public void onComplete() {
		executor.execute(() -> getDownstreamObserver().onComplete());
	}

	@Override
	public void onFail(Throwable t) {
		executor.execute(() -> getDownstreamObserver().onFail(t));
	}
}

package uk.co.strangeskies.osgi;

public class RankedService<T> implements Comparable<RankedService<T>> {
	private final T serviceObject;
	private final int ranking;

	public RankedService(T serviceObject, int ranking) {
		this.serviceObject = serviceObject;
		this.ranking = ranking;
	}

	@Override
	public int hashCode() {
		return serviceObject.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof RankedService<?>))
			return false;

		RankedService<?> that = (RankedService<?>) obj;

		return serviceObject.equals(that.serviceObject);
	}

	public T getServiceObject() {
		return serviceObject;
	}

	public int getRanking() {
		return ranking;
	}

	@Override
	public int compareTo(RankedService<T> o) {
		return ranking - o.ranking;
	}
}

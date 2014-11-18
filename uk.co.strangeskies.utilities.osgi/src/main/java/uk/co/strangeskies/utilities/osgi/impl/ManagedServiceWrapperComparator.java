package uk.co.strangeskies.utilities.osgi.impl;

import uk.co.strangeskies.utilities.IdentityComparator;

class ManagedServiceWrapperComparator extends
		IdentityComparator<ManagedServiceWrapper<?>> {
	@SuppressWarnings("incomplete-switch")
	@Override
	public int compare(ManagedServiceWrapper<?> first,
			ManagedServiceWrapper<?> second) {
		int serviceRankingDifference = first.getServiceRanking()
				- second.getServiceRanking();
		if (serviceRankingDifference != 0) {
			return serviceRankingDifference;
		}

		switch (first.getHideServices()) {
		case ALWAYS:
			switch (second.getHideServices()) {
			case WHEN_WRAPPED:
			case NEVER:
				return 1;
			}
			break;
		case WHEN_WRAPPED:
			switch (second.getHideServices()) {
			case ALWAYS:
				return -1;
			case NEVER:
				return 1;
			}
			break;
		case NEVER:
			switch (second.getHideServices()) {
			case ALWAYS:
			case WHEN_WRAPPED:
				return -1;
			}
			break;
		}

		return super.compare(first, second);
	}
}

/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.utilities.osgi.
 *
 * uk.co.strangeskies.utilities.osgi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities.osgi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.utilities.osgi.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.osgi.impl;

import uk.co.strangeskies.utilities.EqualityComparator;

class ManagedServiceWrapperComparator extends
		EqualityComparator<ManagedServiceWrapper<?>> {
	public ManagedServiceWrapperComparator() {
		super((a, b) -> a == b);
	}

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

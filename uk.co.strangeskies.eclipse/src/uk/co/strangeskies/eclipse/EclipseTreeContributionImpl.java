/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *
 * This file is part of uk.co.strangeskies.eclipse.
 *
 * uk.co.strangeskies.eclipse is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.eclipse is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.eclipse;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import uk.co.strangeskies.fx.TreeContribution;

/**
 * A basic immutable implementation of {@link EclipseTreeContribution}.
 * 
 * @author Elias N Vasylenko
 */
public abstract class EclipseTreeContributionImpl implements EclipseTreeContribution {
	private final List<Class<? extends TreeContribution<?>>> contributions;
	private final Predicate<String> filter;
	private final String id;
	private final int ranking;

	/**
	 * Create an instance with the given values.
	 * 
	 * @param contributions
	 *          the classes of contributions to the tree
	 * @param treeIdFilter
	 *          a predicate to filter which modular trees to contribute to via
	 *          their IDs
	 * @param id
	 *          the ID of the contribution
	 * @param ranking
	 *          the ranking of the contribution
	 */
	public EclipseTreeContributionImpl(Collection<? extends Class<? extends TreeContribution<?>>> contributions,
			Predicate<String> treeIdFilter, String id, int ranking) {
		this.contributions = unmodifiableList(new ArrayList<>(contributions));
		this.filter = treeIdFilter;
		this.id = id == null ? getClass().getSimpleName() : id;
		this.ranking = ranking;
	}

	/**
	 * Create an instance with the default id of the simple class name, and the
	 * default ranking of 0.
	 * 
	 * @param contributions
	 *          the classes of contributions to the tree
	 * @param treeIdFilter
	 *          a predicate to filter which modular trees to contribute to via
	 *          their IDs
	 */
	public EclipseTreeContributionImpl(Collection<? extends Class<? extends TreeContribution<?>>> contributions,
			Predicate<String> treeIdFilter) {
		this(contributions, treeIdFilter, null, 0);
	}

	/**
	 * Create an instance with the default id of the simple class name, the
	 * default ranking of 0, and no filter predicate.
	 * 
	 * @param contributions
	 *          the classes of contributions to the tree
	 */
	public EclipseTreeContributionImpl(Collection<? extends Class<? extends TreeContribution<?>>> contributions) {
		this(contributions, t -> true);
	}

	/**
	 * @param contributions
	 *          the classes of contributions to the tree
	 */
	@SafeVarargs
	public EclipseTreeContributionImpl(Class<? extends TreeContribution<?>>... contributions) {
		this(Arrays.asList(contributions));
	}

	@Override
	public List<Class<? extends TreeContribution<?>>> getContributions(String treeId) {
		return filter.test(treeId) ? contributions : Collections.emptyList();
	}

	@Override
	public String getContributionId() {
		return id;
	}

	@Override
	public int getContributionRanking() {
		return ranking;
	}
}

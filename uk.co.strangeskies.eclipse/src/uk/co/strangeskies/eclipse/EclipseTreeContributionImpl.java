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
	private final int ranking;

	public EclipseTreeContributionImpl(Collection<? extends Class<? extends TreeContribution<?>>> contributions,
			Predicate<String> filter, int ranking) {
		this.contributions = unmodifiableList(new ArrayList<>(contributions));
		this.filter = filter;
		this.ranking = ranking;
	}

	/**
	 * @param contributions
	 *          the classes of contributions to the tree
	 * @param filter TODO@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	 */
	public EclipseTreeContributionImpl(Collection<? extends Class<? extends TreeContribution<?>>> contributions,
			Predicate<String> filter) {
		this(contributions, filter, 0);
	}

	/**
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
}

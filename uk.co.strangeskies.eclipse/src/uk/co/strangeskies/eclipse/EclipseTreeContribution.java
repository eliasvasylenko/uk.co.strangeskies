package uk.co.strangeskies.eclipse;

import java.util.List;

import uk.co.strangeskies.fx.TreeContribution;

/**
 * A source of one or more types of {@link TreeContribution contribution} for
 * the {@link ModularTreeController modular tree controller} of the given id.
 * The contribution classes returned from {@link #getContributions(String)}
 * should be instantiable by Eclipse injector.
 * 
 * @author Elias N Vasylenko
 */
public interface EclipseTreeContribution {
	/**
	 * @param treeId
	 *          the id of the tree to fetch appropriate contribution classes for
	 * @return a collection of contributions to be instantiated by the Eclipse
	 *         context injector on behalf of the {@link ModularTreeController}.
	 */
	List<Class<? extends TreeContribution<?>>> getContributions(String treeId);

	String getContributionId();

	default int getContributionRanking() {
		return 0;
	}
}

package uk.co.strangeskies.fx;

import java.util.Comparator;

import javafx.util.Pair;

/**
 * The default {@link TreeContribution tree contribution} precedence
 * {@link ModularTreeView#setPrecedence(Comparator) comparator}.
 * 
 * @author Elias N Vasylenko
 */
public class DefaultTreeContributionPrecedence implements Comparator<Pair<TreeContribution<?>, Integer>> {
	@Override
	public int compare(Pair<TreeContribution<?>, Integer> first, Pair<TreeContribution<?>, Integer> second) {
		int precedence = second.getValue() - first.getValue();

		if (precedence == 0) {
			if (second.getKey().getDataType().isAssignableFrom(first.getKey().getDataType())) {
				precedence = 1;

			} else if (first.getKey().getDataType().isAssignableFrom(second.getKey().getDataType())) {
				precedence = -1;
			}
		}

		return precedence;
	}
}

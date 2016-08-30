/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *
 * This file is part of uk.co.strangeskies.fx.
 *
 * uk.co.strangeskies.fx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.fx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.fx;

import static java.util.Collections.emptySet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javafx.scene.control.TreeView;
import javafx.util.Pair;
import uk.co.strangeskies.mathematics.graph.Graph;
import uk.co.strangeskies.mathematics.graph.GraphListeners;
import uk.co.strangeskies.mathematics.graph.processing.GraphProcessor;
import uk.co.strangeskies.reflection.TypedObject;

/**
 * An implementation of {@link TreeView} which allows for modular and extensible
 * specification of table structure.
 * 
 * @author Elias N Vasylenko
 */
public class ModularTreeView extends TreeView<TreeItemData<?>> {
	/*
	 * Graph of subtype relations so we can more easily find which contributions
	 * are the most specific.
	 */
	private final Graph<Pair<TreeContribution<?>, Integer>, Object> contributions;
	private final List<TreeContribution<?>> orderedContributions;

	private Comparator<Pair<TreeContribution<?>, Integer>> precedence;

	/**
	 * Instantiate an empty tree view containing the
	 * {@link DefaultTreeCellContribution default cell contribution}, over a cell
	 * factory which instantiates an empty {@link TreeCellImpl}, and according to
	 * the {@link DefaultTreeContributionPrecedence default precedence}.
	 */
	public ModularTreeView() {
		contributions = Graph.build().<Pair<TreeContribution<?>, Integer>>vertices(emptySet()).edgeFactory(Object::new)
				.directed().addInternalListener(GraphListeners::vertexAdded, added -> {
					for (Pair<TreeContribution<?>, Integer> existingVertex : added.graph().vertices()) {
						if (existingVertex != added.vertex()) {}

						int precedence = getPrecedence().compare(existingVertex, added.vertex());

						if (precedence > 0) {
							added.graph().edges().add(added.vertex(), existingVertex);
						} else if (precedence < 0) {
							added.graph().edges().add(existingVertex, added.vertex());
						}
					}

				}).create();
		orderedContributions = new ArrayList<>();

		setPrecedence(new DefaultTreeContributionPrecedence());

		addContribution(new DefaultTreeCellContribution());

		setCellFactory(v -> new TreeCellImpl());
	}

	/**
	 * @param precedence
	 *          the precedence by which to order tree contributions
	 */
	public void setPrecedence(Comparator<Pair<TreeContribution<?>, Integer>> precedence) {
		this.precedence = precedence;

		refreshContributions();
	}

	/**
	 * @return the precedence by which tree contributions are ordered
	 */
	public Comparator<Pair<TreeContribution<?>, Integer>> getPrecedence() {
		return precedence;
	}

	/**
	 * @param root
	 *          the root object supplemented with its exact generic type
	 */
	public void setRootData(TypedObject<?> root) {
		TreeItemImpl<?> rootItem = new TreeItemImpl<>(this, root);
		rootItem.setExpanded(true);
		setShowRoot(false);

		// add root
		setRoot(rootItem);

		refresh();
	}

	protected final TreeItemImpl<?> getRootImpl() {
		return (TreeItemImpl<?>) getRoot();
	}

	/**
	 * As per {@link #addContribution(TreeContribution, int)} with a ranking of 0.
	 * 
	 * @param contribution
	 *          the contribution to add to the view
	 * @return true if the contribution was successfully added, false otherwise
	 */
	public boolean addContribution(TreeContribution<?> contribution) {
		return addContribution(contribution, 0);
	}

	/**
	 * @param contribution
	 *          the contribution to add to the view
	 * @param ranking
	 *          the precedence ranking of the contribution
	 * @return true if the contribution was successfully added, false otherwise
	 */
	public boolean addContribution(TreeContribution<?> contribution, int ranking) {
		boolean added = contributions.vertices().add(new Pair<>(contribution, ranking));

		if (added) {
			refreshContributions();
		}

		return added;
	}

	/**
	 * @param contribution
	 *          the contribution to remove from the view
	 * @return true if the contribution was successfully removed, false otherwise
	 */
	public boolean removeContribution(TreeContribution<?> contribution) {
		boolean removed = contributions.vertices().remove(contribution);

		if (removed) {
			refreshContributions();
		}

		return removed;
	}

	private void refreshContributions() {
		orderedContributions.clear();
		new GraphProcessor().begin(contributions, v -> orderedContributions.add(v.getKey())).processEager();
	}

	/**
	 * @return all contributions added to the view in order of precedence
	 */
	public List<TreeContribution<?>> getContributions() {
		return Collections.unmodifiableList(orderedContributions);
	}

	@Override
	public void refresh() {
		getRootImpl().rebuildChildren();
	}
}

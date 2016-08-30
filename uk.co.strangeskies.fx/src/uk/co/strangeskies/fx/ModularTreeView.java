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

import java.util.Collections;
import java.util.List;

import javafx.scene.control.TreeView;
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
	private final Graph<TreeContribution<?>, Object> contributions;
	private List<TreeContribution<?>> orderedContributions;

	public ModularTreeView() {
		contributions = Graph.build().<TreeContribution<?>> vertices().edgeFactory(Object::new).directed()
				.addInternalListener(GraphListeners::vertexAdded, added -> {
					for (TreeContribution<?> existingVertex : added.graph().vertices())
						if (existingVertex != added.vertex())

							if (existingVertex.getDataType().isAssignableFrom(added.vertex().getDataType())) {
								added.graph().edges().add(added.vertex(), existingVertex);

							} else if (added.vertex().getDataType().isAssignableFrom(existingVertex.getDataType())) {
								added.graph().edges().add(existingVertex, added.vertex());
							}
				}).create();
		orderedContributions = Collections.emptyList();

		addContribution(new DefaultTreeCellContribution());

		setCellFactory(v -> new TreeCellImpl());
	}

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

	public boolean addContribution(TreeContribution<?> contribution) {
		boolean added = contributions.vertices().add(contribution);

		if (added) {
			orderedContributions = new GraphProcessor().begin(contributions).processEager();
		}

		return added;
	}

	public boolean removeContribution(TreeContribution<?> contribution) {
		boolean removed = contributions.vertices().remove(contribution);

		if (removed) {
			orderedContributions = new GraphProcessor().begin(contributions).processEager();
		}

		return removed;
	}

	public List<TreeContribution<?>> getContributions() {
		return Collections.unmodifiableList(orderedContributions);
	}

	@Override
	public void refresh() {
		getRootImpl().rebuildChildren();
	}
}

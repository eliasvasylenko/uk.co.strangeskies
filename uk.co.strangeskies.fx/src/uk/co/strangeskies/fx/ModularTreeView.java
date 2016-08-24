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

import java.util.List;
import java.util.stream.Collectors;

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
public class ModularTreeView extends TreeView<TypedObject<?>> {
	/*
	 * Graph of subtype relations so we can more easily find which contributions
	 * are the most specific.
	 */
	private final Graph<TreeContribution<?>, Object> contributions;

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

		setCellFactory(v -> new TreeCellImpl(this));
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
		return contributions.vertices().add(contribution);
	}

	public boolean removeContribution(TreeContribution<?> contribution) {
		return contributions.vertices().remove(contribution);
	}

	/**
	 * Get all the contributions which should be applied to a tree item, in order
	 * from most to least specific.
	 * 
	 * @param <T>
	 *          the type of the tree item
	 * @param object
	 *          a tree item for which to find contributions
	 * @return the contributions which apply to the given tree item
	 */
	@SuppressWarnings("unchecked")
	public <T> List<TreeContribution<? super T>> getContributions(TypedObject<T> object) {
		List<TreeContribution<?>> orderedContributions = new GraphProcessor().begin(contributions).processEagerParallel();

		return orderedContributions.stream().filter(c -> c.getDataType().isAssignableFrom(object.getType()))
				.map(c -> (TreeContribution<? super T>) c).filter(c -> c.appliesTo(object.getObject()))
				.collect(Collectors.toList());
	}

	@Override
	public void refresh() {
		getRootImpl().rebuildChildren();
	}
}

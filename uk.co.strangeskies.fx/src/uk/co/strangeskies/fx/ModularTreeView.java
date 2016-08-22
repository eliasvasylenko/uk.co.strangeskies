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

	public ModularTreeView(TypedObject<?> root) {
		contributions = Graph.build().<TreeContribution<?>>vertices().edgeFactory(Object::new).directed()
				.addInternalListener(GraphListeners::vertexAdded, e -> {
					for (TreeContribution<?> vertex : e.graph().vertices())
						if (vertex != e.vertex())
							if (vertex.getDataType().isAssignableFrom(e.vertex().getDataType())) {
								e.graph().edges().add(vertex, e.vertex());
							} else if (e.vertex().getDataType().isAssignableFrom(vertex.getDataType())) {
								e.graph().edges().add(e.vertex(), vertex);
							}
				}).create();

		TreeItemImpl<?> rootItem = new TreeItemImpl<>(this, root);
		rootItem.setExpanded(true);

		// add root
		setRoot(rootItem);
		setCellFactory(v -> provideCell());
	}

	protected TreeCellImpl provideCell() {
		return new TreeCellImpl();
	}

	protected final TreeItemImpl<?> getRootImpl() {
		return (TreeItemImpl<?>) getRoot();
	}

	public boolean addContribution(TreeChildContribution<?> childContribution) {
		return contributions.vertices().add(childContribution);
	}

	public boolean removeContribution(TreeChildContribution<?> childContribution) {
		return contributions.vertices().remove(childContribution);
	}

	protected <T> List<TreeContribution<T>> getContributions(TypedObject<T> object) {
		/*-
		
		return childContributions.stream().filter(c -> c.getDataType().isAssignableFrom(value.getType()))
				.map(c -> (TreeChildContribution<T>) c).filter(c -> c.appliesTo(value.getObject()))
				.collect(Collectors.toList());
		
		 */

		return Collections.emptyList();
	}

	@Override
	public void refresh() {
		getRootImpl().rebuildChildren();
	}
}

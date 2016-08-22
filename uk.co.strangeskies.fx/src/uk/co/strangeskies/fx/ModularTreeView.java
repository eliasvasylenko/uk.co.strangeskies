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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.scene.control.TreeView;
import uk.co.strangeskies.mathematics.graph.Graph;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypedObject;

/**
 * An implementation of {@link TreeView} which allows for modular and extensible
 * specification of table structure.
 * 
 * @author Elias N Vasylenko
 */
public class ModularTreeView extends TreeView<TypedObject<?>> {
	new GraphB
	
	private final List<TreeRootContribution> rootContributions;
	private final List<TreeChildContribution<?>> childContributions;
	private final List<TreeCellContribution<?>> cellContributions;

	public ModularTreeView() {
		rootContributions = new ArrayList<>();
		childContributions = new ArrayList<>();
		cellContributions = new ArrayList<>();

		TreeItemImpl<ModularTreeView> root = new TreeItemImpl<ModularTreeView>(this,
				new TypeToken<ModularTreeView>() {}.typedObject(this)) {
			@Override
			public List<TreeChildContribution<ModularTreeView>> getContributions() {
				return rootContributions.stream()
						.map(r -> (TreeChildContribution<ModularTreeView>) new TreeChildContribution<ModularTreeView>() {
							@Override
							public boolean appliesTo(ModularTreeView data) {
								return true;
							}

							@Override
							public boolean hasChildren(ModularTreeView data) {
								return r.hasRootItems();
							}

							@Override
							public List<TypedObject<?>> getChildren(ModularTreeView data) {
								return r.getRootItems();
							}
						}).collect(Collectors.toList());
			}
		};
		root.setExpanded(true);

		// add root
		setShowRoot(false);
		setRoot(root);
		setCellFactory(v -> provideCell());
	}

	protected TreeCellImpl provideCell() {
		return new TreeCellImpl();
	}

	protected final TreeItemImpl<?> getRootImpl() {
		return (TreeItemImpl<?>) getRoot();
	}

	public boolean addRootContribution(TreeRootContribution rootContribution) {
		return rootContributions.add(rootContribution);
	}

	public boolean removeRootContribution(TreeRootContribution rootContribution) {
		return rootContributions.remove(rootContribution);
	}

	public boolean addChildContribution(TreeChildContribution<?> childContribution) {
		return childContributions.add(childContribution);
	}

	public boolean removeChildContribution(TreeChildContribution<?> childContribution) {
		return childContributions.remove(childContribution);
	}

	public List<TypedObject<?>> getChildren(TypedObject<?> data) {
		return childContributions.stream().filter(c -> c.getDataType().isAssignableFrom(value.getType()))
				.map(c -> (TreeChildContribution<T>) c).filter(c -> c.appliesTo(value.getObject()))
				.collect(Collectors.toList());
	}

	public boolean addCellContribution(TreeCellContribution<?> cellContribution) {
		return cellContributions.add(cellContribution);
	}

	public boolean removeCellContribution(TreeCellContribution<?> cellContribution) {
		return cellContributions.remove(cellContribution);
	}

	void configureCell(TypedObject<?> data, TreeCellImpl cell) {
		
	}

	@Override
	public void refresh() {
		getRootImpl().rebuildChildren();
	}
}

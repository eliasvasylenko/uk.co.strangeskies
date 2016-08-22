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

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import uk.co.strangeskies.reflection.TypedObject;

/**
 * Users should not need to extend this class. Item specific behavior should be
 * handled by extending {@link TreeChildContribution} for each type of node
 * which can appear in a tree.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of data for this tree item
 */
public class TreeItemImpl<T> extends TreeItem<TypedObject<?>> {
	private final ModularTreeView treeView;

	private final Map<TypedObject<?>, TreeItemImpl<?>> childTreeItems = new HashMap<>();
	private boolean childrenCalculated;

	TreeItemImpl(ModularTreeView treeView, TypedObject<T> data) {
		super(data);

		this.treeView = treeView;

		if (hasChildren()) {
			expandedProperty().addListener((property, from, to) -> {
				if (!to) {
					childrenCalculated = false;
				}
			});
		} else {
			childrenCalculated = true;
		}
	}

	private boolean hasChildren() {
		return getContributions().stream().filter(TreeChildContribution.class::isInstance)
				.anyMatch(c -> ((TreeChildContribution<T>) c).hasChildren(getData()));
	}

	public List<TreeContribution<T>> getContributions() {
		return treeView.getContributions(getTypedData());
	}

	@SuppressWarnings("unchecked")
	public TypedObject<T> getTypedData() {
		return (TypedObject<T>) getValue();
	}

	@SuppressWarnings("unchecked")
	public T getData() {
		return (T) getValue().getObject();
	}

	@Override
	public ObservableList<TreeItem<TypedObject<?>>> getChildren() {
		if (!childrenCalculated) {
			rebuildChildren();
		}

		return super.getChildren();
	}

	public void rebuildChildren() {
		List<TreeItemImpl<?>> childrenItems;

		if (hasChildren()) {
			List<TypedObject<?>> childrenData = new ArrayList<>();

			getContributions().stream().filter(TreeChildContribution.class::isInstance)
					.map(c -> ((TreeChildContribution<T>) c).getChildren(getData())).forEach(childrenData::addAll);

			// remove outdated TreeItemImpl children
			childTreeItems.keySet().retainAll(childrenData);

			childrenItems = childrenData.stream().map(i -> {
				TreeItemImpl<?> treeItem = childTreeItems.get(i);

				if (treeItem == null) {
					treeItem = new TreeItemImpl<>(treeView, i);
					childTreeItems.put(i, treeItem);
				} else if (isExpanded()) {
					treeItem.rebuildChildren();
				}

				return treeItem;
			}).collect(toList());
		} else {
			childTreeItems.clear();

			childrenItems = Collections.emptyList();
		}

		super.getChildren().setAll(childrenItems);

		childrenCalculated = true;
	}

	@Override
	public boolean isLeaf() {
		if (!childrenCalculated) {
			rebuildChildren();
		}

		return super.isLeaf();
	}
}

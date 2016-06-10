/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.fx.
 *
 * uk.co.strangeskies.fx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.fx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.fx.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.fx;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 * Users should not need to extend this class. Item specific behaviour should be
 * handled by extending {@link TreeItemType} for each type of node which can
 * appear in a tree.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of data for this tree item
 */
public class TreeItemImpl<T> extends TreeItem<TreeItemData<?>> {
	private final Map<TreeItemData<?>, TreeItemImpl<?>> childTreeItems = new HashMap<>();
	private boolean childrenCalculated;

	TreeItemImpl(TreeItemType<T> type, T data) {
		this(new TreeItemData<>(type, data));
	}

	private TreeItemImpl(TreeItemData<T> data) {
		super(data);

		if (getItemType().hasChildren(getData())) {
			expandedProperty().addListener((property, from, to) -> {
				if (!to) {
					childrenCalculated = false;
				}
			});
		} else {
			childrenCalculated = true;
		}
	}

	@SuppressWarnings("unchecked")
	public TreeItemType<T> getItemType() {
		return (TreeItemType<T>) getValue().getItemType();
	}

	@SuppressWarnings("unchecked")
	public T getData() {
		return (T) getValue().getData();
	}

	@Override
	public ObservableList<TreeItem<TreeItemData<?>>> getChildren() {
		if (!childrenCalculated) {
			rebuildChildren();
		}

		return super.getChildren();
	}

	public void rebuildChildren() {
		if (getItemType().hasChildren(getData())) {
			List<TreeItemData<?>> childrenData = new ArrayList<>();
			childrenData.addAll(getItemType().getChildren(getData()));

			// remove outdated TreeItemImpl children
			childTreeItems.keySet().retainAll(childrenData);

			List<TreeItemImpl<?>> childrenItems = childrenData.stream().map(i -> {
				TreeItemImpl<?> treeItem;

				treeItem = childTreeItems.get(i);

				if (treeItem == null) {
					treeItem = new TreeItemImpl<>(i);
					childTreeItems.put(i, treeItem);
				} else if (isExpanded()) {
					treeItem.rebuildChildren();
				}

				return treeItem;
			}).collect(toList());

			super.getChildren().setAll(childrenItems);

			childrenCalculated = true;
		}
	}

	@Override
	public boolean isLeaf() {
		if (!childrenCalculated) {
			rebuildChildren();
		}

		return super.isLeaf();
	}
}

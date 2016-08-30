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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import uk.co.strangeskies.reflection.TypeToken;
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
public class TreeItemImpl<T> extends TreeItem<TreeItemData<?>> {
	private final ModularTreeView treeView;
	private final TreeItemImpl<?> parent;

	private final Map<TypedObject<?>, TreeItemImpl<?>> childTreeItems = new HashMap<>();
	private boolean childrenCalculated;

	TreeItemImpl(ModularTreeView treeView, TypedObject<T> data) {
		this(treeView, data, null);
	}

	TreeItemImpl(ModularTreeView treeView, TypedObject<T> data, TreeItemImpl<?> parent) {
		this.treeView = treeView;
		this.parent = parent;

		setValue(new TreeItemDataImpl<>(data));

		rebuildChildren();
	}

	protected boolean hasChildrenContributions() {
		return getData().contributions(new TypeToken<TreeChildContribution<? super T>>() {}).stream()
				.anyMatch(c -> c.hasChildren(getData()));
	}

	protected List<TypedObject<?>> getChildrenContributions() {
		return getData().contributions(new TypeToken<TreeChildContribution<? super T>>() {}).stream()

				.flatMap(c -> c.getChildren(getData()).stream())

				.distinct()

				.collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	public TreeItemDataImpl<T> getData() {
		return (TreeItemDataImpl<T>) getValue();
	}

	@Override
	public ObservableList<TreeItem<TreeItemData<?>>> getChildren() {
		if (!childrenCalculated) {
			rebuildChildren();
		}

		return super.getChildren();
	}

	public void rebuildChildren() {
		getData().refreshContributions();

		boolean hasChildren = hasChildrenContributions();

		List<TreeItem<TreeItemData<?>>> childrenItems;

		if (hasChildren) {
			if (isExpanded()) {
				List<TypedObject<?>> childrenData = getChildrenContributions();

				// remove outdated TreeItemImpl children
				childTreeItems.keySet().retainAll(childrenData);

				childrenItems = childrenData.stream().map(i -> {
					TreeItemImpl<?> treeItem = childTreeItems.get(i);

					if (treeItem == null) {
						treeItem = new TreeItemImpl<>(treeView, i, this);
						childTreeItems.put(i, treeItem);
					} else {
						treeItem.rebuildChildren();
					}

					return treeItem;
				}).collect(toList());

				childrenCalculated = true;
			} else {
				childrenItems = Arrays.asList(new TreeItem<>());
				childrenCalculated = false;
			}
		} else {
			childTreeItems.clear();

			childrenItems = Collections.emptyList();
			childrenCalculated = true;
		}

		super.getChildren().setAll(childrenItems);
	}

	@Override
	public boolean isLeaf() {
		if (!childrenCalculated) {
			rebuildChildren();
		}

		return super.isLeaf();
	}

	public class TreeItemDataImpl<T> implements TreeItemData<T> {
		private final TypedObject<T> data;

		private List<TreeContribution<? super T>> itemContributions;

		public TreeItemDataImpl(TypedObject<T> data) {
			this.data = data;
			refreshContributions();
		}

		@Override
		public TypedObject<T> typedData() {
			return data;
		}

		@Override
		public Optional<TreeItemData<?>> parent() {
			return Optional.ofNullable(parent).map(TreeItemImpl::getData);
		}

		@SuppressWarnings("unchecked")
		protected void refreshContributions() {
			itemContributions = treeView.getContributions().stream().filter(c -> c.getDataType().isAssignableFrom(type()))
					.map(c -> (TreeContribution<? super T>) c).filter(c -> c.appliesTo(this)).collect(Collectors.toList());
		}

		@Override
		public List<TreeContribution<? super T>> contributions() {
			return new ArrayList<>(itemContributions);
		}

		@Override
		public <U extends TreeContribution<? super T>> List<U> contributions(TypeToken<U> type) {
			return itemContributions.stream().filter(c -> TypeToken.over(type.getRawType()).isAssignableFrom(c.getClass()))
					.map(type::cast).collect(Collectors.toList());
		}
	}
}

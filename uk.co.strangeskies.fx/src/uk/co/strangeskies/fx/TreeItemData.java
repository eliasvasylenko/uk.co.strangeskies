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

import javafx.scene.control.TreeView;

/**
 * This is the type which {@link TreeView} should be parameterized over. This
 * allows for different {@link TreeItemType types} of tree node to appear within
 * the tree hierarchy in a structured manner.
 * <p>
 * Users should not need to extend this class. Item specific behaviour should be
 * handled by extending {@link TreeItemType} for each type of node which can
 * appear in a tree.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of data for this tree item
 */
public class TreeItemData<T> {
	private final TreeItemType<T> type;
	private final T data;

	public TreeItemData(TreeItemType<T> type, T data) {
		this.type = type;
		this.data = data;
	}

	public TreeItemType<T> getItemType() {
		return type;
	}

	public T getData() {
		return data;
	}

	@Override
	public String toString() {
		String text = getItemType().getText(getData());
		String supplemental = getItemType().getSupplementalText(getData());

		if (supplemental != null)
			text += " - " + supplemental;

		return text;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof TreeItemData<?>))
			return false;

		TreeItemData<?> that = (TreeItemData<?>) obj;

		return this.type.equals(that.type) && this.data.equals(that.data);
	}

	@Override
	public int hashCode() {
		return type.hashCode() ^ data.hashCode();
	}
}

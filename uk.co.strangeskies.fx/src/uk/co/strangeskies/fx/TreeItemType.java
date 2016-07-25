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

import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;

/**
 * A type of {@link TreeItemData} for a {@link ModularTreeView}.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of the tree item data
 */
public interface TreeItemType<T> {
	default TypeToken<T> getDataType() {
		return TypeToken.over(getClass()).resolveSupertypeParameters(TreeItemType.class)
				.resolveTypeArgument(new TypeParameter<T>() {}).infer();
	}

	public boolean hasChildren(T data);

	public List<TreeItemData<?>> getChildren(T data);

	default String getText(T data) {
		return data.toString();
	}

	default String getSupplementalText(T data) {
		return null;
	}

	default TreeItemImpl<T> getTreeItem(T data) {
		return new TreeItemImpl<T>(this, data);
	}

	default void configureCell(TreeItemData<T> data, TreeCellImpl cell) {
		cell.name().setText(getText(data.getData()));
		cell.supplemental().setText(getSupplementalText(data.getData()));
	}
}

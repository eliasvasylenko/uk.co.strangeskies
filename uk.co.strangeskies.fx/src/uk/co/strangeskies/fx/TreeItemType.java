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

import java.util.List;

import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;

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

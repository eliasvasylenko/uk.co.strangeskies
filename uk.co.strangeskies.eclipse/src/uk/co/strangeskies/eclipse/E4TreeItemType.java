/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.eclipse.
 *
 * uk.co.strangeskies.eclipse is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.eclipse is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.eclipse.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.eclipse;

import org.eclipse.e4.ui.services.EMenuService;

import uk.co.strangeskies.fx.TreeCellImpl;
import uk.co.strangeskies.fx.TreeItemData;
import uk.co.strangeskies.fx.TreeItemType;

public abstract class E4TreeItemType<T> implements TreeItemType<T> {
	private final EMenuService menuService;
	private final String menuId;

	public E4TreeItemType(EMenuService menuService, String menuId) {
		this.menuService = menuService;
		this.menuId = menuId;
	}

	protected String getMenuId() {
		return menuId;
	}

	protected EMenuService getMenuService() {
		return menuService;
	}

	@Override
	public void configureCell(TreeItemData<T> data, TreeCellImpl cell) {
		TreeItemType.super.configureCell(data, cell);

		if (getMenuId() != null) {
			getMenuService().registerContextMenu(cell, getMenuId());
		}
	}
}

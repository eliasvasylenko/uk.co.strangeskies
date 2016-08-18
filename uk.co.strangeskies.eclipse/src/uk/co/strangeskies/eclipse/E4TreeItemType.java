/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *
 * This file is part of uk.co.strangeskies.eclipse.
 *
 * uk.co.strangeskies.eclipse is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.eclipse is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.eclipse;

import org.eclipse.e4.ui.services.EMenuService;

import uk.co.strangeskies.fx.TreeCellContribution;
import uk.co.strangeskies.fx.TreeCellImpl;

public abstract class E4TreeItemType<T> implements TreeCellContribution<T> {
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
	public void configureCell(T data, String text, String supplementalText, TreeCellImpl cell) {
		TreeCellContribution.super.configureCell(data, text, supplementalText, cell);

		if (getMenuId() != null) {
			getMenuService().registerContextMenu(cell, getMenuId());
		}
	}
}

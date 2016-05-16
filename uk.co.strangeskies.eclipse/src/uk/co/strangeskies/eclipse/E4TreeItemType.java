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

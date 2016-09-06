package uk.co.strangeskies.eclipse;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.services.EMenuService;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import uk.co.strangeskies.fx.TreeCellContribution;
import uk.co.strangeskies.fx.TreeItemData;

public abstract class MenuTreeCellContribution<T> implements TreeCellContribution<T> {
	@Inject
	EMenuService menuService;

	private final String menuId;
	private ContextMenu menu;

	public MenuTreeCellContribution(String menuId) {
		this.menuId = menuId;
	}

	@PostConstruct
	public void configureMenu() {
		Control menuControl = new Control() {};

		menuService.registerContextMenu(menuControl, menuId);

		menu = menuControl.getContextMenu();
		menu.addEventHandler(KeyEvent.ANY, Event::consume);
	}

	@Override
	public <U extends T> Node configureCell(TreeItemData<U> data, Node content) {
		content.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
			menu.show(content, event.getScreenX(), event.getScreenY());
			event.consume();
		});
		content.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
			menu.hide();
		});

		return content;
	}
}

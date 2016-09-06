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

/**
 * A tree cell contribution intended to be supplied via
 * {@link EclipseModularTreeContributor} so as to be injected according to an
 * eclipse context.
 * <p>
 * This contribution registers an E4 popup menu to the cell, which can be
 * activated via right click or the context menu key.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of data of applicable nodes
 */
public abstract class MenuTreeCellContribution<T> implements TreeCellContribution<T> {
	@Inject
	EMenuService menuService;

	private final String menuId;
	private ContextMenu menu;

	/**
	 * @param menuId
	 *          the ID of the popup menu in the E4 model
	 */
	public MenuTreeCellContribution(String menuId) {
		this.menuId = menuId;
	}

	@SuppressWarnings("javadoc")
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

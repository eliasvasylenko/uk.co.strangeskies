package uk.co.strangeskies.eclipse;

import static java.util.Collections.emptyMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import uk.co.strangeskies.fx.TreeCellContribution;
import uk.co.strangeskies.fx.TreeItemData;

/**
 * A tree cell contribution intended to be supplied via
 * {@link EclipseModularTreeContributor} so as to be injected according to an
 * eclipse context.
 * <p>
 * This contribution registers an E4 command to the cell, which can be activated
 * via double click or the enter key.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of data of applicable nodes
 */
public abstract class CommandTreeCellContribution<T> implements TreeCellContribution<T> {
	@Inject
	EHandlerService handlerService;

	private final String commandId;
	private ParameterizedCommand command;

	/**
	 * @param commandId
	 *          the ID of the command in the E4 model
	 */
	public CommandTreeCellContribution(String commandId) {
		this.commandId = commandId;
	}

	@SuppressWarnings("javadoc")
	@PostConstruct
	public void configureCommand(ECommandService commandService) {
		command = commandService.createCommand(commandId, emptyMap());
	}

	@Override
	public <U extends T> Node configureCell(TreeItemData<U> data, Node content) {
		content.addEventHandler(MouseEvent.ANY, event -> {
			if (event.getClickCount() == 2 && event.getButton().equals(MouseButton.PRIMARY)) {
				event.consume();

				if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
					executeCommand();
				}
			}
		});

		content.addEventHandler(KeyEvent.ANY, event -> {
			if (event.getCode() == KeyCode.ENTER) {
				event.consume();

				if (event.getEventType().equals(KeyEvent.KEY_PRESSED)) {
					executeCommand();
				}
			}
		});

		return content;
	}

	private void executeCommand() {
		handlerService.executeHandler(command);
	}
}

package uk.co.strangeskies.fx;

import static uk.co.strangeskies.fx.FXMLLoadBuilder.buildWith;
import static uk.co.strangeskies.fx.FXUtilities.getResource;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;

/**
 * A basic tree cell implementation for {@link TreeItem}. This class may be
 * extended to provide further functionality, e.g. as {@link E4TreeCellImpl}
 * does to streamline use in an Eclipse E4 application.
 * 
 * @author Elias N Vasylenko
 */
public class TreeCellImpl extends TreeCell<TreeItemData<?>> {
	@FXML
	private Node graphic;
	@FXML
	private Label name;
	@FXML
	private Label supplemental;

	public TreeCellImpl() {
		this(new FXMLLoader());
	}

	public TreeCellImpl(FXMLLoader loader) {
		buildWith(loader).object(this).resource(getResource(TreeCellImpl.class)).load();
	}

	@Override
	protected void updateItem(TreeItemData<?> item, boolean empty) {
		super.updateItem(item, empty);

		if (empty || item == null) {
			clearItem();
		} else {
			updateItem(item);
		}
	}

	protected void clearItem() {
		setGraphic(null);
		name.setText(null);
		supplemental.setText(null);
	}

	public Label name() {
		return name;
	}

	public Label supplemental() {
		return supplemental;
	}

	protected <T> void updateItem(TreeItemData<T> item) {
		setGraphic(graphic);
		item.getItemType().configureCell(item, this);
	}
}

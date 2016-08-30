package uk.co.strangeskies.fx;

import static uk.co.strangeskies.fx.FXMLLoadBuilder.build;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class DefaultTreeCellContent extends HBox {
	@FXML
	private Label text;
	@FXML
	private Label supplementalText;

	public DefaultTreeCellContent(String text, String supplementalText) {
		build().object(this).load();

		this.text.setText(text);
		this.supplementalText.setText(supplementalText);
	}
}

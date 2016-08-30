package uk.co.strangeskies.fx;

import java.util.Objects;

import javafx.scene.Node;
import uk.co.strangeskies.reflection.TypeToken;

/**
 * The default tree cell contribution. This configures a cell with basic text
 * content derived from any applicable {@link TreeTextContribution text
 * contributions}.
 * 
 * @author Elias N Vasylenko
 */
public class DefaultTreeCellContribution implements TreeCellContribution<Object> {
	@Override
	public <U> Node configureCell(TreeItemData<U> data, Node ignore) {
		String text = null;
		String supplementalText = null;

		for (TreeTextContribution<? super U> contribution : data
				.contributions(new TypeToken<TreeTextContribution<? super U>>() {})) {

			if (text == null) {
				text = contribution.getText(data);
			}
			if (supplementalText == null) {
				supplementalText = contribution.getSupplementalText(data);
			}
		}

		if (text == null) {
			text = Objects.toString(data.data());
		}

		return new DefaultTreeCellContent(text, supplementalText);
	}
}

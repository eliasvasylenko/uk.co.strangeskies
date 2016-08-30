package uk.co.strangeskies.fx;

import javafx.css.PseudoClass;
import javafx.scene.Node;

public interface PseudoClassTreeCellContribution<T> extends TreeCellContribution<T> {
	@Override
	default <U extends T> Node configureCell(TreeItemData<U> data, Node content) {
		content.pseudoClassStateChanged(PseudoClass.getPseudoClass(getPseudoClassName()), true);
		return content;
	}

	default String getPseudoClassName() {
		return getClass().getSimpleName();
	}
}

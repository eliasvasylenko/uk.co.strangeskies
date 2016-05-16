package uk.co.strangeskies.fx;

import javafx.scene.control.TreeView;

/**
 * This is the type which {@link TreeView} should be parameterized over. This
 * allows for different {@link TreeItemType types} of tree node to appear within
 * the tree hierarchy in a structured manner.
 * <p>
 * Users should not need to extend this class. Item specific behaviour should be
 * handled by extending {@link TreeItemType} for each type of node which can
 * appear in a tree.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of data for this tree item
 */
public class TreeItemData<T> {
	private final TreeItemType<T> type;
	private final T data;

	public TreeItemData(TreeItemType<T> type, T data) {
		this.type = type;
		this.data = data;
	}

	public TreeItemType<T> getItemType() {
		return type;
	}

	public T getData() {
		return data;
	}

	@Override
	public String toString() {
		String text = getItemType().getText(getData());
		String supplemental = getItemType().getSupplementalText(getData());

		if (supplemental != null)
			text += " - " + supplemental;

		return text;
	}
}

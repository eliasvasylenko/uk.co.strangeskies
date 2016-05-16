package uk.co.strangeskies.fx;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 * Users should not need to extend this class. Item specific behaviour should be
 * handled by extending {@link TreeItemType} for each type of node which can
 * appear in a tree.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of data for this tree item
 */
public class TreeItemImpl<T> extends TreeItem<TreeItemData<?>> {
	private boolean childrenCalculated;

	TreeItemImpl(TreeItemType<T> type, T data) {
		this(new TreeItemData<>(type, data));
	}

	private TreeItemImpl(TreeItemData<T> data) {
		super(data);

		if (getItemType().hasChildren(getData())) {
			expandedProperty().addListener((property, from, to) -> {
				if (!to) {
					childrenCalculated = false;
				}
			});
		} else {
			childrenCalculated = true;
		}
	}

	@SuppressWarnings("unchecked")
	public TreeItemType<T> getItemType() {
		return (TreeItemType<T>) getValue().getItemType();
	}

	@SuppressWarnings("unchecked")
	public T getData() {
		return (T) getValue().getData();
	}

	@Override
	public ObservableList<TreeItem<TreeItemData<?>>> getChildren() {
		if (!childrenCalculated) {
			rebuildChildren();
		}

		return super.getChildren();
	}

	public void rebuildChildren() {
		if (getItemType().hasChildren(getData())) {
			List<TreeItemData<?>> children = new ArrayList<>();
			children.addAll(getItemType().getChildren(getData()));
			super.getChildren().setAll(children.stream().map(i -> new TreeItemImpl<>(i)).collect(toList()));
			childrenCalculated = true;
		}
	}

	@Override
	public boolean isLeaf() {
		if (!childrenCalculated) {
			rebuildChildren();
		}

		return super.isLeaf();
	}
}

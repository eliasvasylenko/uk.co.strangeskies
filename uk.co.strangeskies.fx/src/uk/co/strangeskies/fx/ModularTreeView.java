package uk.co.strangeskies.fx;

import javafx.scene.control.TreeView;

/**
 * An implementation of {@link TreeView} which allows for modular and extensible
 * specification of table structure.
 * 
 * @author Elias N Vasylenko
 */
public class ModularTreeView extends TreeView<TreeItemData<?>> {
	public final TreeItemImpl<?> getRootImpl() {
		return (TreeItemImpl<?>) getRoot();
	}
}

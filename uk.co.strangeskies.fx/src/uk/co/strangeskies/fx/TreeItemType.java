package uk.co.strangeskies.fx;

import java.util.List;

import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;

public interface TreeItemType<T> {
	default TypeToken<T> getDataType() {
		return TypeToken.over(getClass()).resolveSupertypeParameters(TreeItemType.class)
				.resolveTypeArgument(new TypeParameter<T>() {}).infer();
	}

	public boolean hasChildren(T data);

	public List<TreeItemData<?>> getChildren(T data);

	default String getText(T data) {
		return data.toString();
	}

	default String getSupplementalText(T data) {
		return null;
	}

	default TreeItemImpl<T> getTreeItem(T data) {
		return new TreeItemImpl<T>(this, data);
	}

	default void configureCell(TreeItemData<T> data, TreeCellImpl cell) {
		cell.name().setText(getText(data.getData()));
		cell.supplemental().setText(getSupplementalText(data.getData()));
	}
}

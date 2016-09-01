/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *
 * This file is part of uk.co.strangeskies.fx.
 *
 * uk.co.strangeskies.fx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.fx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.fx;

import static uk.co.strangeskies.fx.FXMLLoadBuilder.build;
import static uk.co.strangeskies.fx.FXUtilities.getResource;

import java.util.ArrayList;
import java.util.Collections;

import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;
import uk.co.strangeskies.reflection.TypeToken;

/**
 * A basic tree cell implementation for {@link TreeItem}. This class may be
 * extended to provide further functionality.
 * 
 * @author Elias N Vasylenko
 */
public class TreeCellImpl extends TreeCell<TreeItemData<?>> {
	/**
	 * Load a new instance from the FXML located according to
	 * {@link FXUtilities#getResource(Class)} for this class.
	 */
	public TreeCellImpl() {
		build().object(this).resource(getResource(TreeCellImpl.class)).load();
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
	}

	protected <T> void updateItem(TreeItemData<T> item) {
		ArrayList<TreeCellContribution<? super T>> contributions = new ArrayList<>(
				item.contributions(new TypeToken<TreeCellContribution<? super T>>() {}));

		Collections.reverse(contributions);

		Node content = new HBox();

		for (TreeCellContribution<? super T> contribution : contributions) {
			content = contribution.configureCell(item, content);
		}

		setGraphic(content);
	}
}

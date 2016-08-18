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

import static uk.co.strangeskies.fx.FXMLLoadBuilder.buildWith;
import static uk.co.strangeskies.fx.FXUtilities.getResource;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import uk.co.strangeskies.reflection.TypedObject;

/**
 * A basic tree cell implementation for {@link TreeItem}. This class may be
 * extended to provide further functionality.
 * 
 * @author Elias N Vasylenko
 */
public class TreeCellImpl extends TreeCell<TypedObject<?>> {
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
	protected void updateItem(TypedObject<?> item, boolean empty) {
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

	protected <T> void updateItem(TypedObject<T> item) {
		setGraphic(graphic);
		// item.getContributions().get(0).configureCell(item, this); TODO
	}
}

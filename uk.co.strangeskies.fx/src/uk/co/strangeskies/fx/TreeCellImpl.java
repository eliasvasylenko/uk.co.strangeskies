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
import java.util.List;

import javafx.css.PseudoClass;
import javafx.fxml.FXML;
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
	private final ModularTreeView view;

	private List<? extends TreeContribution<?>> contributions;

	@FXML
	private Node graphic;
	@FXML
	private Label name;
	@FXML
	private Label supplemental;

	public TreeCellImpl(ModularTreeView view) {
		this.view = view;

		build().object(this).resource(getResource(TreeCellImpl.class)).load();
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
		deconfigure();

		setGraphic(null);
		name.setText(null);
		supplemental.setText(null);
	}

	private void deconfigure() {
		if (contributions != null) {
			for (TreeContribution<?> contribution : this.contributions) {
				if (contribution instanceof TreeCellContribution<?>) {
					pseudoClassStateChanged(PseudoClass.getPseudoClass(contribution.getClass().getSimpleName()), false);

					((TreeCellContribution<?>) contribution).deconfigureCell(this);
				}
			}
			contributions = null;
		}
	}

	public Node defaultGraphic() {
		return graphic;
	}

	public Label text() {
		return name;
	}

	public Label supplemental() {
		return supplemental;
	}

	@SuppressWarnings("unchecked")
	protected <T> void updateItem(TypedObject<T> item) {
		deconfigure();

		ArrayList<TreeContribution<? super T>> contributions = new ArrayList<>(view.getContributions(item));
		contributions.add(new DefaultTreeCellTextContribution());
		Collections.reverse(contributions);

		this.contributions = contributions;

		String text = null;
		String supplemental = null;

		for (TreeContribution<?> contribution : contributions) {
			if (contribution instanceof TreeTextContribution<?>) {
				text = ((TreeTextContribution<? super T>) contribution).getText(item.getObject());
				supplemental = ((TreeTextContribution<? super T>) contribution).getSupplementalText(item.getObject());
			}

			if (contribution instanceof TreeCellContribution<?>) {
				pseudoClassStateChanged(PseudoClass.getPseudoClass(contribution.getClass().getSimpleName()), true);

				((TreeCellContribution<? super T>) contribution).configureCell(item.getObject(), text, supplemental, this);
			}
		}
	}
}

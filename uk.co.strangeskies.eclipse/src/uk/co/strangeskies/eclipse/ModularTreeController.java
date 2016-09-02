/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *
 * This file is part of uk.co.strangeskies.eclipse.
 *
 * uk.co.strangeskies.eclipse is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.eclipse is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.eclipse;

import java.util.function.Predicate;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import uk.co.strangeskies.fx.ModularTreeView;
import uk.co.strangeskies.fx.TreeContribution;
import uk.co.strangeskies.fx.TreeItemData;
import uk.co.strangeskies.fx.TreeItemImpl;

/**
 * An interface part for interacting with the experiment workspace via a modular
 * tree model.
 * 
 * @author Elias N Vasylenko
 */
public class ModularTreeController {
	private final StringProperty tableId = new SimpleStringProperty();
	private final Predicate<String> filter;

	@FXML
	private ModularTreeView modularTree;

	@Inject
	IEclipseContext context;

	@Inject
	@ObservableService
	ObservableList<EclipseTreeContribution> contributions;

	/**
	 * A controller with the default id - the simple name of the class - and no
	 * contribution filter.
	 */
	public ModularTreeController() {
		tableId.set(getClass().getSimpleName());
		filter = null;
	}

	/**
	 * @param id
	 *          the {@link #getId() ID} of the controller to create
	 */
	public ModularTreeController(String id) {
		tableId.set(id);
		this.filter = null;
	}

	/**
	 * @param id
	 *          the {@link #getId() ID} of the controller to create
	 * @param filter
	 *          a filter over the IDs of which {@link EclipseTreeContribution
	 *          contributions} to accept contributions from
	 */
	public ModularTreeController(String id, Predicate<String> filter) {
		tableId.set(id);
		this.filter = filter;
	}

	@FXML
	void initialize() {
		contributions.addListener((ListChangeListener<EclipseTreeContribution>) change -> {
			while (change.next())
				if (change.wasAdded())
					change.getAddedSubList().forEach(this::contribute);
		});

		contributions.stream().forEach(this::contribute);
	}

	/**
	 * @return The ID property of the controller. This is used to allow
	 *         {@link EclipseTreeContribution contributions} to filter which
	 *         controllers they wish to contribute to.
	 */
	public StringProperty getTableIdProperty() {
		return tableId;
	}

	/**
	 * @return the current ID of the controller
	 */
	public String getId() {
		return tableId.get();
	}

	/**
	 * @param id
	 *          the new ID for the controller
	 */
	public void setId(String id) {
		tableId.set(id);
	}

	protected void contribute(EclipseTreeContribution contributor) {
		if (filter == null || filter.test(contributor.getContributionId()))
			for (Class<? extends TreeContribution<?>> contribution : contributor.getContributions(getId()))
				modularTree.addContribution(ContextInjectionFactory.make(contribution, context));
	}

	/**
	 * @return the modular tree view instance
	 */
	public ModularTreeView getTreeView() {
		return modularTree;
	}

	/**
	 * @return the currently selected tree item
	 */
	public TreeItemImpl<?> getSelection() {
		return (TreeItemImpl<?>) modularTree.getSelectionModel().getSelectedItem();
	}

	/**
	 * @return the currently selected tree item data
	 */
	public TreeItemData<?> getSelectionData() {
		return getSelection().getValue();
	}
}
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
public abstract class ModularTreeController {
	private final String id;
	private final Predicate<String> filter;

	@FXML
	private ModularTreeView treeView;

	@Inject
	IEclipseContext context;

	@Inject
	@ObservableService
	ObservableList<EclipseTreeContribution> contributions;

	/**
	 * @param id
	 *          the {@link #getId() ID} of the controller to create
	 */
	public ModularTreeController(String id) {
		this(id, c -> true);
	}

	/**
	 * @param id
	 *          the {@link #getId() ID} of the controller to create
	 * @param filter
	 *          a filter over the IDs of which {@link EclipseTreeContribution
	 *          contributions} to accept contributions from
	 */
	public ModularTreeController(String id, Predicate<String> filter) {
		this.id = id;
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
	 * @return The ID of the controller. This is used to allow
	 *         {@link EclipseTreeContribution contributions} to filter which
	 *         controllers they wish to contribute to.
	 */
	public String getId() {
		return id;
	}

	protected void contribute(EclipseTreeContribution contributor) {
		if (filter.test(contributor.getContributionId()))
			for (Class<? extends TreeContribution<?>> contribution : contributor.getContributions(getId()))
				treeView.addContribution(ContextInjectionFactory.make(contribution, context));
	}

	/**
	 * @return the modular tree view instance
	 */
	public ModularTreeView getTreeView() {
		return treeView;
	}

	/**
	 * @return the currently selected tree item
	 */
	public TreeItemImpl<?> getSelection() {
		return (TreeItemImpl<?>) treeView.getSelectionModel().getSelectedItem();
	}

	/**
	 * @return the currently selected tree item data
	 */
	public TreeItemData<?> getSelectionData() {
		return getSelection().getValue();
	}
}

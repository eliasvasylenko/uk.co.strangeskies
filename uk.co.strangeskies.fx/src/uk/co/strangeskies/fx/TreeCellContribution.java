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

/**
 * A type of contribution for items in a {@link ModularTreeView}.
 * 
 * Very loosely based on ideas from:
 * {@link "http://help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Fguide%2Fcnf.htm"}
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of the tree item data
 */
public interface TreeCellContribution<T> extends TreeContribution<T> {
	default String getText(T data) {
		return data.toString();
	}

	default String getSupplementalText(T data) {
		return null;
	}

	/**
	 * Used to change the default cell configuration strategy. Implementations may
	 * likely wish to invoke {@link #getText(Object)} or
	 * {@link #getSupplementalText(Object)} to determine proper text decoration.
	 * <p>
	 * Here is also a good place to mark a cell with a pseudo-class to flag for
	 * custom css styling.
	 * 
	 * @param data
	 *          the data contents of the cell
	 * @param cell
	 *          the cell object this item contribution is being applied to
	 */
	default void configureCell(T data, String text, String supplementalText, TreeCellImpl cell) {
		cell.name().setText(text);
		cell.supplemental().setText(supplementalText);
	}
}

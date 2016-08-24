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

public class DefaultTreeCellTextContribution implements TreeCellContribution<Object>, TreeTextContribution<Object> {
	@Override
	public String getText(Object data) {
		return data.toString();
	}

	@Override
	public String getSupplementalText(Object data) {
		return null;
	}

	@Override
	public void configureCell(Object data, String text, String supplementalText, TreeCellImpl cell) {
		cell.setGraphic(cell.defaultGraphic());
		TreeTextContribution.super.configureCell(data, text, supplementalText, cell);
	}
}

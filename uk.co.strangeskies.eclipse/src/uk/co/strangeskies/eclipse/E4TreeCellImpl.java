/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.eclipse.
 *
 * uk.co.strangeskies.eclipse is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.eclipse is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.eclipse.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.eclipse;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.fx.core.di.LocalInstance;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.TreeItem;
import uk.co.strangeskies.fx.TreeCellImpl;

/**
 * A basic tree cell implementation for {@link TreeItem} for e(fx)clipse
 * applications.
 * 
 * @author Elias N Vasylenko
 */
@Creatable
public class E4TreeCellImpl extends TreeCellImpl {
	/**
	 * @param loader
	 *          the FXML loader given by e(fx)clipse
	 */
	@Inject
	public E4TreeCellImpl(@LocalInstance FXMLLoader loader) {
		super(loader);
	}
}

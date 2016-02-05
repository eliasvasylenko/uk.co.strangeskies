/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.p2.
 *
 * uk.co.strangeskies.p2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.p2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.p2.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.p2.impl;

import org.eclipse.core.runtime.IProgressMonitor;

class ProgressMonitorImpl implements IProgressMonitor {
	private boolean cancelled = false;

	@Override
	public void beginTask(String name, int totalWork) {}

	@Override
	public void done() {}

	@Override
	public void internalWorked(double work) {}

	@Override
	public boolean isCanceled() {
		return cancelled;
	}

	@Override
	public void setCanceled(boolean value) {
		cancelled = value;
	}

	@Override
	public void setTaskName(String name) {}

	@Override
	public void subTask(String name) {
		System.out.println("  " + name);
	}

	@Override
	public void worked(int work) {}
}

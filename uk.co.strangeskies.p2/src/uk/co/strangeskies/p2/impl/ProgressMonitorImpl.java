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

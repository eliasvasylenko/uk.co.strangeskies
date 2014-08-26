package uk.co.strangeskies.gears.utilities.flowcontrol;

public class RunQueueInterruptedException extends InterruptedException {
	private static final long serialVersionUID = 8617182418363226996L;

	private long position;

	public RunQueueInterruptedException(InterruptedException cause, long position) {
		this.position = position;
	}

	public long getPosition() {
		return position;
	}
}

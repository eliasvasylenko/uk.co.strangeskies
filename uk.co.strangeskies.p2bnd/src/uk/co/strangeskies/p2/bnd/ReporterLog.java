package uk.co.strangeskies.p2.bnd;

import aQute.service.reporter.Reporter;
import uk.co.strangeskies.utilities.Log;

public class ReporterLog implements Log {
	private final Reporter reporter;

	public ReporterLog(Reporter reporter) {
		this.reporter = reporter;
	}

	@Override
	public void log(Level level, String message) {
		switch (level) {
		case TRACE:
		case DEBUG:
		case INFO:
			reporter.trace("%s", message);
			break;
		case WARN:
			reporter.warning("%s", message);
			break;
		case ERROR:
			reporter.error("%s", message);
			break;
		}
	}
}

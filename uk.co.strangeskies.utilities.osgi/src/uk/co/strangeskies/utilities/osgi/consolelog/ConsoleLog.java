package uk.co.strangeskies.utilities.osgi.consolelog;

import java.util.function.Function;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

/**
 * {@link LogListener} implementation dumping all logs to console
 * 
 * @author Elias N Vasylenko
 */
@Component(immediate = true)
public class ConsoleLog implements LogListener {
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	@SuppressWarnings("javadoc")
	public void addLogReader(LogReaderService service) {
		service.addLogListener(this);
	}

	@SuppressWarnings("javadoc")
	public void removeLogReader(LogReaderService service) {
		service.removeLogListener(this);
	}

	@Override
	public void logged(LogEntry entry) {
		String time = formatTime(entry.getTime());
		String level = formatLevel(entry.getLevel());
		String bundle = formatIfPresent(entry.getBundle());
		String service = formatIfPresent(entry.getServiceReference());

		System.out.println(
				"[" + time + level + bundle + service + "] " + entry.getMessage());
	}

	private String formatTime(long time) {
		return Long.toString(time);
	}

	private String formatLevel(int level) {
		String levelString;

		switch (level) {
		case LogService.LOG_ERROR:
			levelString = "ERROR  ";
			break;
		case LogService.LOG_WARNING:
			levelString = "WARNING";
			break;
		case LogService.LOG_INFO:
			levelString = "INFO   ";
			break;
		case LogService.LOG_DEBUG:
			levelString = "DEBUG  ";
			break;
		default:
			throw new IllegalArgumentException("Unexpected log level");
		}

		return "; " + levelString;
	}

	private String formatIfPresent(Object object) {
		return formatIfPresent(object, Object::toString);
	}

	private String formatIfPresent(Object object,
			Function<Object, String> format) {
		return object != null ? "; " + format.apply(object) : "";
	}
}

package uk.co.strangeskies.mathematics.graph;

import static uk.co.strangeskies.text.properties.PropertyLoader.getDefaultProperties;

import java.util.function.Function;

import uk.co.strangeskies.text.properties.Localized;
import uk.co.strangeskies.text.properties.LocalizedRuntimeException;

public class GraphException extends LocalizedRuntimeException {
	public GraphException(Localized<String> message) {
		super(message);
	}

	public GraphException(Localized<String> message, Throwable cause) {
		super(message, cause);
	}

	public GraphException(Function<GraphProperties, Localized<String>> message) {
		this(message.apply(getDefaultProperties(GraphProperties.class)));
	}

	public GraphException(Function<GraphProperties, Localized<String>> message, Throwable cause) {
		this(message.apply(getDefaultProperties(GraphProperties.class)), cause);
	}
}

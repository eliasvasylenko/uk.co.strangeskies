package uk.co.strangeskies.osgi.logging;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@SuppressWarnings("javadoc")
@ObjectClassDefinition(name = "Console Log Configuration", description = "The console log provides a listener over the OSGi log service which spits output to stdout")
public @interface ConsoleLogConfiguration {
	@AttributeDefinition(name = "Enable Console Log", description = "Enable console output for the OSGi log service")
	boolean enabled();
}

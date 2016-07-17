package uk.co.strangeskies.mathematics.graph;

import uk.co.strangeskies.text.properties.Localized;
import uk.co.strangeskies.text.properties.Properties;

public interface GraphProperties extends Properties<GraphProperties> {
	Localized<String> alreadyConfigured(Object item);

	Localized<String> edgeMap();

	Localized<String> edgeVertices();

	Localized<String> edgeFactory();

	Localized<String> edgeMultiFactory();

	Localized<String> edgeWeight();

	Localized<String> edgeEquality();

	Localized<String> vertexEquality();

	Localized<String> direction();

	Localized<String> directionFunction();
}

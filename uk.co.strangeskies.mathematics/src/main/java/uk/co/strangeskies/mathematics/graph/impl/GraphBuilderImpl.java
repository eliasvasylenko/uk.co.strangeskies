package uk.co.strangeskies.mathematics.graph.impl;

import uk.co.strangeskies.mathematics.graph.building.GraphBuilder;
import uk.co.strangeskies.mathematics.graph.building.GraphConfigurator;

public class GraphBuilderImpl implements GraphBuilder {
	@Override
	public GraphConfigurator<Object, Object> configure() {
		return GraphConfiguratorImpl.configure();
	}
}

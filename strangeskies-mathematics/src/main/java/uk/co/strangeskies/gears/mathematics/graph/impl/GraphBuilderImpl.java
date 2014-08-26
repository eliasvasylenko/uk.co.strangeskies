package uk.co.strangeskies.gears.mathematics.graph.impl;

import uk.co.strangeskies.gears.mathematics.graph.building.GraphBuilder;
import uk.co.strangeskies.gears.mathematics.graph.building.GraphConfigurator;

public class GraphBuilderImpl implements GraphBuilder {
	@Override
	public GraphConfigurator<Object, Object> configure() {
		return GraphConfiguratorImpl.configure();
	}
}

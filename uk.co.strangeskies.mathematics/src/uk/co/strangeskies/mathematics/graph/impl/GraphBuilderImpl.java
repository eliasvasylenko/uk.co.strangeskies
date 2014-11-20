package uk.co.strangeskies.mathematics.graph.impl;

import org.osgi.service.component.annotations.Component;

import uk.co.strangeskies.mathematics.graph.building.GraphBuilder;
import uk.co.strangeskies.mathematics.graph.building.GraphConfigurator;

@Component
public class GraphBuilderImpl implements GraphBuilder {
	@Override
	public GraphConfigurator<Object, Object> configure() {
		return GraphConfiguratorImpl.configure();
	}
}

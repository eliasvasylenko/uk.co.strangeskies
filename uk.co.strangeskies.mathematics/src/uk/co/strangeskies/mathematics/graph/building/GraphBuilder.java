package uk.co.strangeskies.mathematics.graph.building;

import uk.co.strangeskies.mathematics.graph.Graph;
import uk.co.strangeskies.utilities.factory.Builder;

public interface GraphBuilder extends
		Builder<GraphConfigurator<Object, Object>, Graph<Object, Object>> {
	@Override
	public GraphConfigurator<Object, Object> configure();
}

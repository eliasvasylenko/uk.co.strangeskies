package uk.co.strangeskies.gears.mathematics.graph.building;

import uk.co.strangeskies.gears.mathematics.graph.Graph;
import uk.co.strangeskies.gears.utilities.factory.Builder;

public interface GraphBuilder extends
		Builder<GraphConfigurator<Object, Object>, Graph<Object, Object>> {
	@Override
	public GraphConfigurator<Object, Object> configure();
}

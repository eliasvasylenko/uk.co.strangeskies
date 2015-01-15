package uk.co.strangeskies.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

public interface CaptureConversion {
	public ParameterizedType getOriginalType();

	public ParameterizedType getCapturedType();

	public Set<InferenceVariable> getInferenceVariables();

	public Type getCapturedType(InferenceVariable variable);
}

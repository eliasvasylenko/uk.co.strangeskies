package uk.co.strangeskies.reflection;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MethodDeclaration<C, T> extends ParameterizedDeclaration implements MemberDeclaration<C, T> {
	private final ClassDefinition<C> classDefinition;
	private final String methodName;

	private final Map<VariableExpressionProxy<?>, AnnotatedType> parameters;
	private AnnotatedType returnType;

	protected MethodDeclaration(ClassDefinition<C> classDefinition, String methodName) {
		this.classDefinition = classDefinition;
		this.methodName = methodName;

		parameters = new LinkedHashMap<>();
	}

	@Override
	public ClassDefinition<C> getClassDefinition() {
		return classDefinition;
	}

	public Set<VariableExpressionProxy<?>> getParameters() {
		return parameters.keySet();
	}

	public AnnotatedType getParameterType(VariableExpressionProxy<?> parameter) {
		return parameters.get(parameter);
	}

	@Override
	public String getName() {
		return methodName;
	}

	public AnnotatedType getReturnType() {
		return returnType;
	}

	@Override
	public MethodDefinition<C, T> define() {
		return new MethodDefinition<>(this);
	}

	public VariableExpression<?> addParameter(AnnotatedType type) {
		VariableExpressionProxy<?> proxy = new VariableExpressionProxy<>();
		parameters.put(proxy, type);
		return proxy;
	}

	public VariableExpression<?> addParameter(Type type) {
		return addParameter(AnnotatedTypes.over(type));
	}

	@SuppressWarnings("unchecked")
	public <U> VariableExpression<U> addParameter(Class<U> type) {
		return (VariableExpression<U>) addParameter(AnnotatedTypes.over(type));
	}

	@SuppressWarnings("unchecked")
	public <U> VariableExpression<U> addParameter(TypeToken<U> type) {
		return (VariableExpression<U>) addParameter(type.getAnnotatedDeclaration());
	}

	public MethodDeclaration<C, T> withParameters(Collection<AnnotatedType> types) {
		for (AnnotatedType type : types) {
			addParameter(type);
		}
		return this;
	}

	public MethodDeclaration<C, T> withParameters(AnnotatedType... types) {
		return withParameters(Arrays.asList(types));
	}

	public MethodDeclaration<C, T> withParameters(Type... types) {
		return withParameters(AnnotatedTypes.over(types));
	}

	public MethodDeclaration<C, T> withParameters(TypeToken<?>... types) {
		return withParameters(Arrays.stream(types).map(TypeToken::getAnnotatedDeclaration).collect(Collectors.toList()));
	}

	public MethodDeclaration<C, T> withReturnType(AnnotatedType type) {
		returnType = type;
		return this;
	}

	public MethodDeclaration<C, T> withReturnType(Type type) {
		return withReturnType(AnnotatedTypes.over(type));
	}

	@SuppressWarnings("unchecked")
	public <U extends T> MethodDeclaration<C, U> withReturnType(Class<U> type) {
		return (MethodDeclaration<C, U>) withReturnType(AnnotatedTypes.over(type));
	}

	@SuppressWarnings("unchecked")
	public <U extends T> MethodDeclaration<C, U> withReturnType(TypeToken<U> type) {
		return (MethodDeclaration<C, U>) withReturnType(type.getAnnotatedDeclaration());
	}
}

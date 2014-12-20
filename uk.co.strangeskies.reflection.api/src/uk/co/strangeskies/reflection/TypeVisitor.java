package uk.co.strangeskies.reflection;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class TypeVisitor {
	private final Set<Type> visited = new HashSet<>();

	public final void visit(Type... types) {
		visit(Arrays.asList(types));
	}

	public final void visit(Collection<? extends Type> types) {
		for (Type type : types)
			if (visited.add(type))
				if (type instanceof Class)
					visitClass((Class<?>) type);
				else if (type instanceof ParameterizedType)
					visitParameterizedType((ParameterizedType) type);
				else if (type instanceof GenericArrayType)
					visitGenericArrayType((GenericArrayType) type);
				else if (type instanceof WildcardType)
					visitWildcardType((WildcardType) type);
				else if (type instanceof TypeVariable)
					visitTypeVariable((TypeVariable<?>) type);
				else if (type instanceof IntersectionType)
					visitIntersectionType((IntersectionType) type);
				else if (type == null)
					visitNull();
				else
					throw new AssertionError("Unknown type: " + type + " of class "
							+ type.getClass());
	}

	protected void visitNull() {}

	protected void visitClass(Class<?> type) {}

	protected void visitParameterizedType(ParameterizedType type) {}

	protected void visitGenericArrayType(GenericArrayType type) {}

	protected void visitWildcardType(WildcardType type) {}

	protected void visitTypeVariable(TypeVariable<?> type) {}

	protected void visitIntersectionType(IntersectionType type) {}
}

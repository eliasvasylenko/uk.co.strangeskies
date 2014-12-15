package uk.co.strangeskies.reflection;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * An implementation of TypeVisitor which provides recursion over the related
 * types specified by the arguments passed to constructor. Supertypes of
 * parameterised types are visited without those type arguments passed through.
 * 
 * This means that if the type List<String> is visited, the supertype
 * Collections<E> will be visited subsequently, rather than Collection<String>.
 * If the type String is visited, on the other hand, the supertype
 * Comparable<String> will be visited. If the raw type List is visited, then the
 * raw supertype Collection will be visited subsequently.
 * 
 * @author eli
 *
 */
public abstract class RecursiveTypeVisitor extends TypeVisitor {
	private final boolean supertypes;
	private final boolean enclosed;
	private final boolean enclosing;
	private final boolean parameters;
	private final boolean bounds;

	/**
	 * This defaults to recursion over parameters, bounds and enclosing types, but
	 * not supertypes or enclosed types.
	 */
	public RecursiveTypeVisitor() {
		this(false, false, true, true, true);
	}

	public RecursiveTypeVisitor(boolean supertypes, boolean enclosed,
			boolean enclosing, boolean parameters, boolean bounds) {
		this.supertypes = supertypes;
		this.enclosed = enclosed;
		this.enclosing = enclosing;
		this.parameters = parameters;
		this.bounds = bounds;
	}

	@Override
	protected void visitClass(Class<?> type) {
		visit(type.getComponentType());

		if (supertypes) {
			visit(type.getSuperclass());
			visit(type.getInterfaces());
		}
		if (parameters)
			visit(type.getTypeParameters());
		if (enclosed)
			visit(type.getClasses());
		if (enclosing)
			visit(type.getEnclosingClass());
	}

	@Override
	protected void visitGenericArrayType(GenericArrayType type) {
		visit(type.getGenericComponentType());
	}

	@Override
	protected void visitParameterizedType(ParameterizedType type) {
		if (supertypes) {
			visit(((Class<?>) type.getRawType()).getGenericSuperclass());
			visit(((Class<?>) type.getRawType()).getGenericInterfaces());
		}
		if (parameters)
			visit(type.getActualTypeArguments());
		if (enclosed)
			visit(((Class<?>) type.getRawType()).getClasses());
		if (enclosing)
			visit(type.getOwnerType());
	}

	@Override
	protected void visitTypeVariable(TypeVariable<?> type) {
		if (bounds)
			visit(type.getBounds());
	}

	@Override
	protected void visitWildcardType(WildcardType type) {
		if (bounds) {
			visit(type.getLowerBounds());
			visit(type.getUpperBounds());
		}
	}

	@Override
	protected void visitIntersectionType(IntersectionType type) {
		visit(type.getTypes());
	}
}

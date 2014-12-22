package uk.co.strangeskies.reflection;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.function.Consumer;

import uk.co.strangeskies.utilities.factory.Configurator;

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
public class RecursiveTypeVisitor extends TypeVisitor {
	public static class Builder extends Configurator<RecursiveTypeVisitor> {
		private boolean allowRepeatVisits = false;

		private boolean supertypes = false;
		private boolean enclosed = false;
		private boolean enclosing = false;
		private boolean parameters = false;
		private boolean bounds = false;

		private boolean postOrder = false;

		private Consumer<Class<?>> classVisitor = t -> {};
		private Consumer<GenericArrayType> genericArrayVisitor = t -> {};
		private Consumer<ParameterizedType> parameterizedTypeVisitor = t -> {};
		private Consumer<TypeVariable<?>> typeVariableVisitor = t -> {};
		private Consumer<WildcardType> wildcardVisitor = t -> {};
		private Consumer<IntersectionType> intersectionTypeVisitor = t -> {};

		private Builder() {}

		@Override
		protected RecursiveTypeVisitor tryCreate() {
			return new RecursiveTypeVisitor(allowRepeatVisits, supertypes, enclosed,
					enclosing, parameters, bounds, postOrder, classVisitor,
					genericArrayVisitor, parameterizedTypeVisitor, typeVariableVisitor,
					wildcardVisitor, intersectionTypeVisitor);
		}

		public Builder allowRepeatVisits(boolean allowRepeatVisits) {
			this.allowRepeatVisits = allowRepeatVisits;
			return this;
		}

		public Builder visitSupertypes() {
			this.supertypes = true;
			return this;
		}

		public Builder visitEnclosedTypes() {
			this.enclosed = true;
			return this;
		}

		public Builder visitEnclosingTypes() {
			this.enclosing = true;
			return this;
		}

		public Builder visitParameters() {
			this.parameters = true;
			return this;
		}

		public Builder visitBounds() {
			this.bounds = true;
			return this;
		}

		public Builder postOrder() {
			this.postOrder = true;
			return this;
		}

		public Builder preOrder() {
			this.postOrder = false;
			return this;
		}

		public Builder classVisitor(Consumer<Class<?>> classVisitor) {
			this.classVisitor = classVisitor;
			return this;
		}

		public Builder genericArrayVisitor(
				Consumer<GenericArrayType> genericArrayVisitor) {
			this.genericArrayVisitor = genericArrayVisitor;
			return this;
		}

		public Builder parameterizedTypeVisitor(
				Consumer<ParameterizedType> parameterizedTypeVisitor) {
			this.parameterizedTypeVisitor = parameterizedTypeVisitor;
			return this;
		}

		public Builder typeVariableVisitor(
				Consumer<TypeVariable<?>> typeVariableVisitor) {
			this.typeVariableVisitor = typeVariableVisitor;
			return this;
		}

		public Builder wildcardVisitor(Consumer<WildcardType> wildcardVisitor) {
			this.wildcardVisitor = wildcardVisitor;
			return this;
		}

		public Builder intersectionTypeVisitor(
				Consumer<IntersectionType> intersectionTypeVisitor) {
			this.intersectionTypeVisitor = intersectionTypeVisitor;
			return this;
		}
	}

	private final boolean supertypes;
	private final boolean enclosed;
	private final boolean enclosing;
	private final boolean parameters;
	private final boolean bounds;

	private final boolean postOrder;

	private final Consumer<Class<?>> classVisitor;
	private final Consumer<GenericArrayType> genericArrayVisitor;
	private final Consumer<ParameterizedType> parameterizedTypeVisitor;
	private final Consumer<TypeVariable<?>> typeVariableVisitor;
	private final Consumer<WildcardType> wildcardVisitor;
	private final Consumer<IntersectionType> intersectionTypeVisitor;

	private RecursiveTypeVisitor(boolean allowRepeatVisits, boolean supertypes,
			boolean enclosed, boolean enclosing, boolean parameters, boolean bounds,
			boolean postOrder, Consumer<Class<?>> classVisitor,
			Consumer<GenericArrayType> genericArrayVisitor,
			Consumer<ParameterizedType> parameterizedTypeVisitor,
			Consumer<TypeVariable<?>> typeVariableVisitor,
			Consumer<WildcardType> wildcardVisitor,
			Consumer<IntersectionType> intersectionTypeVisitor) {
		super(allowRepeatVisits);

		this.supertypes = supertypes;
		this.enclosed = enclosed;
		this.enclosing = enclosing;
		this.parameters = parameters;
		this.bounds = bounds;

		this.postOrder = postOrder;

		this.classVisitor = classVisitor;
		this.genericArrayVisitor = genericArrayVisitor;
		this.parameterizedTypeVisitor = parameterizedTypeVisitor;
		this.typeVariableVisitor = typeVariableVisitor;
		this.wildcardVisitor = wildcardVisitor;
		this.intersectionTypeVisitor = intersectionTypeVisitor;
	}

	public static Builder build() {
		return new Builder();
	}

	@Override
	protected void visitClass(Class<?> type) {
		if (!postOrder)
			classVisitor.accept(type);

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

		if (postOrder)
			classVisitor.accept(type);
	}

	@Override
	protected void visitGenericArrayType(GenericArrayType type) {
		if (!postOrder)
			genericArrayVisitor.accept(type);

		visit(type.getGenericComponentType());

		if (postOrder)
			genericArrayVisitor.accept(type);
	}

	@Override
	protected void visitParameterizedType(ParameterizedType type) {
		if (!postOrder)
			parameterizedTypeVisitor.accept(type);

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

		if (postOrder)
			parameterizedTypeVisitor.accept(type);
	}

	@Override
	protected void visitTypeVariable(TypeVariable<?> type) {
		if (!postOrder)
			typeVariableVisitor.accept(type);

		if (bounds)
			visit(type.getBounds());

		if (postOrder)
			typeVariableVisitor.accept(type);
	}

	@Override
	protected void visitWildcardType(WildcardType type) {
		if (!postOrder)
			wildcardVisitor.accept(type);

		if (bounds) {
			visit(type.getLowerBounds());
			visit(type.getUpperBounds());
		}

		if (postOrder)
			wildcardVisitor.accept(type);
	}

	@Override
	protected void visitIntersectionType(IntersectionType type) {
		if (!postOrder)
			intersectionTypeVisitor.accept(type);

		visit(type.getTypes());

		if (postOrder)
			intersectionTypeVisitor.accept(type);
	}
}

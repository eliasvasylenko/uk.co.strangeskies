package uk.co.strangeskies.reflection;

import java.lang.reflect.AnnotatedType;

public class FieldDeclaration<C, T> implements MemberDeclaration<C, T> {
	private final ClassDefinition<C> classDefinition;
	private final String fieldName;

	private AnnotatedType type;

	private ValueExpression<? extends T> initializer;

	protected FieldDeclaration(ClassDefinition<C> classDefinition, String fieldName, AnnotatedType type) {
		this.classDefinition = classDefinition;
		this.fieldName = fieldName;
		this.type = type;
	}

	@Override
	public ClassDefinition<C> getClassDefinition() {
		return classDefinition;
	}

	@Override
	public String getName() {
		return fieldName;
	}

	public AnnotatedType getType() {
		return type;
	}

	@Override
	public FieldDefinition<C, T> define() {
		return new FieldDefinition<>(this);
	}

	public ValueExpression<? extends T> getInitializer() {
		return initializer;
	}
}

package uk.co.strangeskies.reflection;

public class FieldDefinition<C, T> implements MemberDefinition {
	private final ClassDefinition<C> classDefinition;
	private final String fieldName;

	private final TypeToken<T> type;
	private final ValueExpression<? extends T> initializer;

	protected FieldDefinition(FieldDeclaration<C, T> declaration) {
		this.classDefinition = declaration.getClassDefinition();
		this.fieldName = declaration.getName();
		this.initializer = declaration.getInitializer();

		if (declaration.getType() == null) {
			this.type = (TypeToken<T>) initializer.getType();
		} else {
			this.type = (TypeToken<T>) TypeToken.over(declaration.getType());
		}
	}

	@Override
	public ClassDefinition<C> getDeclaringClassDefinition() {
		return classDefinition;
	}

	@Override
	public String getName() {
		return fieldName;
	}

	@Override
	public int getModifiers() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isSynthetic() {
		// TODO Auto-generated method stub
		return false;
	}

}

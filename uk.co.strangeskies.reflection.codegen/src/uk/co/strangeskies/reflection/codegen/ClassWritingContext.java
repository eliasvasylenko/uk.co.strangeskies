package uk.co.strangeskies.reflection.codegen;

import static org.objectweb.asm.Type.getInternalName;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import org.objectweb.asm.signature.SignatureVisitor;

class ClassWritingContext {
	private ClassWritingContext() {}

	public static void visitTypeSignature(SignatureVisitor visitor, Type type) {
		if (type instanceof Class<?>) {
			visitClassSignature(visitor, (Class<?>) type);
		} else if (type instanceof ParameterizedType) {
			visitParameterizedTypeSignature(visitor, (ParameterizedType) type);
		} else if (type instanceof GenericArrayType) {
			visitGenericArrayTypeSignature(visitor, (GenericArrayType) type);
		} else if (type instanceof TypeVariable<?>) {
			visitTypeVariableSignature(visitor, (TypeVariable<?>) type);
		} else if (type instanceof TypeVariableSignature.Reference) {
			visitTypeVariableSignature(visitor, (TypeVariableSignature.Reference) type);
		} else {
			throw new IllegalArgumentException();
		}
	}

	private static void visitClassSignature(SignatureVisitor visitor, Class<?> type) {
		while (type.isArray()) {
			visitor.visitArrayType();
			type = type.getComponentType();
		}
		visitor.visitClassType(getInternalName(type));
		visitor.visitEnd();
	}

	private static void visitParameterizedTypeSignature(
			SignatureVisitor visitor,
			ParameterizedType type) {
		visitor.visitClassType(getInternalName((Class<?>) type.getRawType()));
		for (Type argument : type.getActualTypeArguments()) {
			if (argument instanceof WildcardType) {
				WildcardType wildcard = (WildcardType) argument;
				if (wildcard.getUpperBounds().length > 0) {
					visitor.visitTypeArgument('+');
					for (Type bound : wildcard.getUpperBounds())
						visitTypeSignature(visitor, bound);
				} else if (wildcard.getLowerBounds().length > 0) {
					visitor.visitTypeArgument('-');
					for (Type bound : wildcard.getUpperBounds())
						visitTypeSignature(visitor, bound);
				} else {
					visitor.visitTypeArgument();
				}
			} else {
				visitor.visitTypeArgument('=');
				visitTypeSignature(visitor, argument);
			}
		}
		visitor.visitEnd();
	}

	private static void visitGenericArrayTypeSignature(
			SignatureVisitor visitor,
			GenericArrayType type) {
		Type component;

		do {
			visitor.visitArrayType();
			component = type.getGenericComponentType();

			if (!(component instanceof GenericArrayType))
				break;

			type = (GenericArrayType) component;
		} while (true);

		visitParameterizedTypeSignature(visitor, (ParameterizedType) component);
	}

	private static void visitTypeVariableSignature(SignatureVisitor visitor, TypeVariable<?> type) {
		visitTypeVariableSignature(visitor, type.getTypeName());
	}

	private static void visitTypeVariableSignature(
			SignatureVisitor visitor,
			TypeVariableSignature.Reference type) {
		visitTypeVariableSignature(visitor, type.getTypeName());
	}

	private static void visitTypeVariableSignature(SignatureVisitor visitor, String typeName) {
		visitor.visitTypeVariable(typeName);
	}
}

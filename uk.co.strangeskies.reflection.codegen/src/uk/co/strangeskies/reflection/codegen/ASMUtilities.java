package uk.co.strangeskies.reflection.codegen;

import static org.objectweb.asm.Type.getInternalName;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.objectweb.asm.signature.SignatureVisitor;

class ASMUtilities {
	private ASMUtilities() {}

	public static void visitTypeSignature(SignatureVisitor visitor, Type type) {
		if (type instanceof Class<?>) {
			visitClassSignature(visitor, (Class<?>) type);
		} else if (type instanceof ParameterizedType) {
			visitParameterizedTypeSignature(visitor, (ParameterizedType) type);
		} else if (type instanceof GenericArrayType) {
			visitGenericArrayTypeSignature(visitor, (GenericArrayType) type);
		} else if (type instanceof TypeVariable<?>) {
			visitTypeVariableSignature(visitor, (TypeVariable<?>) type);
		}
	}

	private static void visitClassSignature(
			SignatureVisitor visitor,
			Class<?> type) {
		while (type.isArray()) {
			visitor.visitArrayType();
			type = type.getComponentType();
		}
		visitor.visitClassType(getInternalName(type));
	}

	private static void visitParameterizedTypeSignature(
			SignatureVisitor visitor,
			ParameterizedType type) {
		visitor.visitClassType(getInternalName((Class<?>) type.getRawType()));
		for (Type argument : type.getActualTypeArguments()) {
			visitor.visitTypeArgument();
			visitTypeSignature(visitor, argument);
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

	private static void visitTypeVariableSignature(
			SignatureVisitor visitor,
			TypeVariable<?> type) {
		visitor.visitTypeVariable(type.getName());
	}
}

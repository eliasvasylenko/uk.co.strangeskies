package uk.co.strangeskies.reflection.codegen;

import static org.objectweb.asm.Type.getInternalName;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.signature.SignatureVisitor;

class ClassWritingContext {
	private static final Map<Class<?>, Character> PRIMITIVE_DESCRIPTORS = Collections
			.unmodifiableMap(new HashMap<Class<?>, Character>() {
				private static final long serialVersionUID = 1L;

				{
					put(void.class, 'V');
					put(boolean.class, 'Z');
					put(byte.class, 'B');
					put(char.class, 'C');
					put(short.class, 'S');
					put(int.class, 'S');
					put(long.class, 'J');
					put(float.class, 'F');
					put(double.class, 'D');
				}
			});

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
			visitor = visitor.visitArrayType();
			type = type.getComponentType();
		}

		if (PRIMITIVE_DESCRIPTORS.containsKey(type)) {
			visitor.visitBaseType(PRIMITIVE_DESCRIPTORS.get(type));
		} else {
			visitor.visitClassType(getInternalName(type));
			visitor.visitEnd();
		}
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
			visitor = visitor.visitArrayType();
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

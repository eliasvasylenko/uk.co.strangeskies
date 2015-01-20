package uk.co.strangeskies.reflection;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

public class GenericArrayTypes {
	private GenericArrayTypes() {}

	public static GenericArrayType fromComponentType(Type type) {
		return new GenericArrayType() {
			@Override
			public Type getGenericComponentType() {
				return type;
			}

			@Override
			public String toString() {
				return Types.toString(type)
						+ (type instanceof IntersectionType ? " " : "") + "[]";
			}

			@Override
			public boolean equals(Object object) {
				if (this == object)
					return true;
				if (object == null || !(object instanceof GenericArrayType))
					return false;

				GenericArrayType that = (GenericArrayType) object;

				return type.equals(that.getGenericComponentType());
			}

			@Override
			public int hashCode() {
				return type.hashCode();
			}
		};
	}
}

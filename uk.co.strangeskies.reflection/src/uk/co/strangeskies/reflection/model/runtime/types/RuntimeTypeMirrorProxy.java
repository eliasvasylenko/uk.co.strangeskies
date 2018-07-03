package uk.co.strangeskies.reflection.model.runtime.types;

import uk.co.strangeskies.reflection.model.TypeMirrorProxy;

public interface RuntimeTypeMirrorProxy extends TypeMirrorProxy, RuntimeTypeMirror,
    RuntimeExecutableType, RuntimeUnionType, RuntimeIntersectionType, RuntimePrimitiveType,
    RuntimeNoType, RuntimeReferenceType, RuntimeArrayType, RuntimeDeclaredType, RuntimeErrorType,
    RuntimeNullType, RuntimeTypeVariable, RuntimeWildcardType {

}

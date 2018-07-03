package uk.co.strangeskies.reflection.model.runtime.types.impl;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import uk.co.strangeskies.reflection.model.runtime.impl.RuntimeTypesImpl;
import uk.co.strangeskies.reflection.model.runtime.types.ReifiableRuntimeType;

public class ReflectionArrayTypeImpl extends RuntimeTypeMirrorImpl
    implements javax.lang.model.type.ArrayType, ReifiableRuntimeType {
  private Class<?> source = null;
  private Class<?> component = null;
  private TypeMirror eagerComponent = null;

  public ReflectionArrayTypeImpl(Class<?> source) {
    super(TypeKind.ARRAY);
    this.source = source;
    this.component = source.getComponentType();
    this.eagerComponent = TypeFactory.instance(component);
  }

  @Override
  public TypeMirror getComponentType() {
    return eagerComponent;
  }

  @Override
  public Class<?> getSource() {
    return source;
  }

  @Override
  public List<? extends TypeMirror> directSuperTypes() {
    final TypeMirror componentType = getComponentType();
    final TypeMirror[] directSupers;

    // JLS v4 4.10.3
    if (componentType.getKind().isPrimitive() || component.equals(java.lang.Object.class)) {
      directSupers = new TypeMirror[3];
      directSupers[0] = TypeFactory.instance(java.lang.Object.class);
      directSupers[1] = TypeFactory.instance(java.lang.Cloneable.class);
      directSupers[2] = TypeFactory.instance(java.io.Serializable.class);
    } else if (componentType.getKind() == TypeKind.ARRAY) {
      List<? extends TypeMirror> componentDirectSupertypes = RuntimeTypesImpl
          .instance()
          .directSupertypes(componentType);
      directSupers = new TypeMirror[componentDirectSupertypes.size()];
      for (int i = 0; i < directSupers.length; i++) {
        directSupers[i] = new ReflectionArrayTypeImpl(
            Array
                .newInstance(
                    ((ReifiableRuntimeType) componentDirectSupertypes.get(i)).getSource(),
                    0)
                .getClass());
      }
    } else {
      Class<?> superClass = component.getSuperclass();
      Class<?>[] interfaces = component.getInterfaces();
      directSupers = new TypeMirror[1 + interfaces.length];

      directSupers[0] = TypeFactory.instance(Array.newInstance(superClass, 0).getClass());

      for (int i = 0; i < interfaces.length; i++) {
        directSupers[i + 1] = TypeFactory.instance(Array.newInstance(interfaces[i], 0).getClass());
      }
    }

    return Collections.unmodifiableList(Arrays.asList(directSupers));
  }

  @Override
  public String toString() {
    return getKind() + " of " + getComponentType().toString();
  }
}
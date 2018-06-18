package uk.co.strangeskies.reflection.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

class ReflectionTypeElementImpl extends ReflectionElementImpl implements ReflectionTypeElement {
  private final Class<?> source;

  protected ReflectionTypeElementImpl(Class<?> source) {
    Objects.requireNonNull(source);
    if (source.isPrimitive() || source.isArray()) {
      throw new IllegalArgumentException(
          "Cannot create a ReflectionTypeElement based on class: " + source);
    }

    this.source = source;
  }

  @Override
  public TypeMirror asType() {
    return CoreReflectionFactory.createTypeMirror(source);
  }

  @Override
  public Class<?> getSource() {
    return source;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ReflectionTypeElementImpl) {
      return source.equals(((ReflectionTypeElementImpl) o).getSource());
    } else {
      return false;
    }
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitType(this, p);
  }

  @Override
  public <R, P> R accept(ReflectionElementVisitor<R, P> v, P p) {
    return v.visitType(this, p);
  }

  @Override
  public Set<Modifier> getModifiers() {
    return ModifierUtil
        .instance(
            source.getModifiers() & (source.isInterface()
                ? java.lang.reflect.Modifier.interfaceModifiers()
                : java.lang.reflect.Modifier.classModifiers()),
            false);
  }

  @Override
  public List<ReflectionElement> getEnclosedElements() {
    List<ReflectionElement> enclosedElements = new ArrayList<>();

    for (Class<?> declaredClass : source.getDeclaredClasses()) {
      enclosedElements.add(CoreReflectionFactory.createMirror(declaredClass));
    }

    // Add elements in the conventional ordering: fields, then
    // constructors, then methods.
    for (Field f : source.getDeclaredFields()) {
      enclosedElements.add(CoreReflectionFactory.createMirror(f));
    }

    for (Constructor<?> c : source.getDeclaredConstructors()) {
      enclosedElements.add(CoreReflectionFactory.createMirror(c));
    }

    for (Method m : source.getDeclaredMethods()) {
      enclosedElements.add(CoreReflectionFactory.createMirror(m));
    }

    return (enclosedElements.isEmpty()
        ? Collections.emptyList()
        : Collections.unmodifiableList(enclosedElements));
  }

  // Review for default method handling.
  @Override
  public List<ReflectionElement> getAllMembers() {
    List<ReflectionElement> allMembers = new ArrayList<>();

    // If I only had a MultiMap ...
    List<ReflectionElement> fields = new ArrayList<>();
    List<ReflectionExecutableElement> methods = new ArrayList<>();
    List<ReflectionElement> classes = new ArrayList<>();

    // Add all fields for this class
    for (Field f : source.getDeclaredFields()) {
      fields.add(CoreReflectionFactory.createMirror(f));
    }

    // Add all methods for this class
    for (Method m : source.getDeclaredMethods()) {
      methods.add(CoreReflectionFactory.createMirror(m));
    }

    // Add all classes for this class, except anonymous/local as per
    // Elements.getAllMembers doc
    for (Class<?> c : source.getDeclaredClasses()) {
      if (c.isLocalClass() || c.isAnonymousClass())
        continue;
      classes.add(CoreReflectionFactory.createMirror(c));
    }

    Class<?> cls = source;
    if (cls.isInterface()) {
      cls = null;
    }
    do {
      // Walk up superclasses adding non-private elements.
      // If source is an interface, just add Object's
      // elements.

      if (cls == null) {
        cls = java.lang.Object.class;
      } else {
        cls = cls.getSuperclass();
      }

      addMembers(cls, fields, methods, classes);

    } while (cls != java.lang.Object.class);

    // add members on (super)interface(s)
    Set<Class<?>> seenInterfaces = new HashSet<>();
    Queue<Class<?>> interfaces = new LinkedList<>();
    if (source.isInterface()) {
      seenInterfaces.add(source);
      interfaces.add(source);
    } else {
      Class<?>[] ifaces = source.getInterfaces();
      for (Class<?> iface : ifaces) {
        seenInterfaces.add(iface);
        interfaces.add(iface);
      }
    }

    while (interfaces.peek() != null) {
      Class<?> head = interfaces.remove();
      addMembers(head, fields, methods, classes);

      Class<?>[] ifaces = head.getInterfaces();
      for (Class<?> iface : ifaces) {
        if (!seenInterfaces.contains(iface)) {
          seenInterfaces.add(iface);
          interfaces.add(iface);
        }
      }
    }

    // Add constructors
    for (Constructor<?> c : source.getDeclaredConstructors()) {
      allMembers.add(CoreReflectionFactory.createMirror(c));
    }

    // Add all unique methods
    allMembers.addAll(methods);

    // Add all unique fields
    allMembers.addAll(fields);

    // Add all unique classes
    allMembers.addAll(classes);

    return Collections.unmodifiableList(allMembers);
  }

  private void addMembers(
      Class<?> cls,
      List<ReflectionElement> fields,
      List<ReflectionExecutableElement> methods,
      List<ReflectionElement> classes) {
    Elements elements = CoreReflectionFactory.getElements();

    for (Field f : cls.getDeclaredFields()) {
      if (java.lang.reflect.Modifier.isPrivate(f.getModifiers())) {
        continue;
      }
      ReflectionElement tmp = CoreReflectionFactory.createMirror(f);
      boolean add = true;
      for (ReflectionElement e : fields) {
        if (elements.hides(e, tmp)) {
          add = false;
          break;
        }
      }
      if (add) {
        fields.add(tmp);
      }
    }

    for (Method m : cls.getDeclaredMethods()) {
      if (java.lang.reflect.Modifier.isPrivate(m.getModifiers()))
        continue;

      ReflectionExecutableElement tmp = CoreReflectionFactory.createMirror(m);
      boolean add = true;
      for (ReflectionExecutableElement e : methods) {
        if (elements.hides(e, tmp)) {
          add = false;
          break;
        } else if (elements.overrides(e, tmp, this)) {
          add = false;
          break;
        }
      }
      if (add) {
        methods.add(tmp);
      }
    }

    for (Class<?> c : cls.getDeclaredClasses()) {
      if (java.lang.reflect.Modifier.isPrivate(c.getModifiers()) || c.isLocalClass()
          || c.isAnonymousClass())
        continue;

      ReflectionElement tmp = CoreReflectionFactory.createMirror(c);
      boolean add = true;
      for (ReflectionElement e : classes) {
        if (elements.hides(e, tmp)) {
          add = false;
          break;
        }
      }
      if (add) {
        classes.add(tmp);
      }
    }
  }

  @Override
  public ElementKind getKind() {
    if (source.isInterface()) {
      if (source.isAnnotation())
        return ElementKind.ANNOTATION_TYPE;
      else
        return ElementKind.INTERFACE;
    } else if (source.isEnum()) {
      return ElementKind.ENUM;
    } else
      return ElementKind.CLASS;
  }

  @Override
  public NestingKind getNestingKind() {
    if (source.isAnonymousClass())
      return NestingKind.ANONYMOUS;
    else if (source.isLocalClass())
      return NestingKind.LOCAL;
    else if (source.isMemberClass())
      return NestingKind.MEMBER;
    else
      return NestingKind.TOP_LEVEL;
  }

  @Override
  public Name getQualifiedName() {
    String name = source.getCanonicalName(); // TODO, this should be a FQN for
                                             // the current element
    if (name == null)
      name = "";
    return StringName.instance(name);
  }

  @Override
  public Name getSimpleName() {
    return StringName.instance(source.getSimpleName());
  }

  @Override
  public TypeMirror getSuperclass() {
    if (source.equals(java.lang.Object.class)) {
      return ReflectionNoType.getNoneInstance();
    } else {
      return CoreReflectionFactory.createTypeMirror(source.getSuperclass());
    }
  }

  @Override
  public List<? extends TypeMirror> getInterfaces() {
    Class<?>[] interfaces = source.getInterfaces();
    int len = interfaces.length;
    List<TypeMirror> res = new ArrayList<>(len);

    if (len > 0) {
      for (Class<?> c : interfaces) {
        res.add(CoreReflectionFactory.createTypeMirror(c));
      }
    } else {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(res);
  }

  @Override
  public List<ReflectionTypeParameterElement> getTypeParameters() {
    return ReflectionTypeParameterElementImpl.createTypeParameterList(source);
  }

  @Override
  public ReflectionElement getEnclosingElement() {
    // Returns the package of a top-level type and returns the
    // immediately lexically enclosing element for a nested type.

    switch (getNestingKind()) {
    case TOP_LEVEL:
      return CoreReflectionFactory.createMirror(source.getPackage());
    case MEMBER:
      return CoreReflectionFactory.createMirror(source.getEnclosingClass());
    default:
      if (source.getEnclosingConstructor() != null) {
        return CoreReflectionFactory.createMirror(source.getEnclosingConstructor());
      } else if (source.getEnclosingMethod() != null) {
        return CoreReflectionFactory.createMirror(source.getEnclosingMethod());
      } else {
        return CoreReflectionFactory.createMirror(source.getEnclosingClass());
      }
    }
  }

  @Override
  public Name getBinaryName() {
    return StringName.instance(getSource().getName());
  }
}
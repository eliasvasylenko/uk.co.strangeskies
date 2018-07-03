package uk.co.strangeskies.reflection.model.runtime.elements.impl;

import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner9;
import javax.lang.model.util.Elements;

import sun.reflect.generics.factory.CoreReflectionFactory;
import uk.co.strangeskies.reflection.model.runtime.RuntimeElements;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeElement;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeExecutableElement;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimePackageElement;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeTypeElement;
import uk.co.strangeskies.reflection.model.runtime.impl.StringName;

public class ReflectionElementsImpl implements RuntimeElements {
  private ReflectionElementsImpl() {} // mostly one instance for you

  private static ReflectionElementsImpl instance = new ReflectionElementsImpl();

  static ReflectionElementsImpl instance() {
    return instance;
  }

  @Override
  public RuntimePackageElement getPackageElement(CharSequence name) {
    return CoreReflectionFactory.asMirror(Package.getPackage(name.toString()));
  }

  @Override
  public RuntimeTypeElement getTypeElement(CharSequence name) {
    // where name is a Canonical Name jls 6.7
    // but this method will probably accept an equivalent FQN
    // depending on Class.forName(String)

    RuntimeTypeElement tmp = null;

    // Filter out arrays
    String n = name.toString();
    if (n.contains("["))
      return null;
    if (n.equals(""))
      return null;

    // The intention of this loop is to handle nested
    // elements. If finding the element using Class.forName
    // fails, an attempt is made to find the element as an
    // enclosed element by trying fo find a prefix of the name
    // (dropping a trailing ".xyz") and looking for "xyz" as
    // an enclosed element.

    Deque<String> parts = new ArrayDeque<>();
    boolean again;
    do {
      again = false;
      try {
        tmp = CoreReflectionFactory.asMirror(Class.forName(n));
      } catch (ClassNotFoundException e) {
        tmp = null;
      }

      if (tmp != null) {
        if (parts.isEmpty()) {
          return tmp;
        }

        tmp = findInner(tmp, parts);
        if (tmp != null) {
          return tmp;
        }
      }

      int indx = n.lastIndexOf('.');
      if (indx > -1) {
        parts.addFirst(n.substring(indx + 1));
        n = n.substring(0, indx);
        again = true;
      }
    } while (again);

    return null;
  }

  // Recursively finds enclosed type elements named as part.top() popping part and
  // repeating
  private RuntimeTypeElement findInner(RuntimeTypeElement e, Deque<String> parts) {
    if (parts.isEmpty()) {
      return e;
    }

    String part = parts.removeFirst();
    List<RuntimeElement> enclosed = e.getEnclosedElements();
    for (RuntimeElement elm : enclosed) {
      if ((elm.getKind() == ElementKind.CLASS || elm.getKind() == ElementKind.INTERFACE
          || elm.getKind() == ElementKind.ENUM || elm.getKind() == ElementKind.ANNOTATION_TYPE)
          && elm.getSimpleName().toString().equals(part)) {
        RuntimeTypeElement t = findInner((RuntimeTypeElement) elm, parts);
        if (t != null) {
          return t;
        }
      }
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<? extends RuntimeExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults(
      AnnotationMirror a) {
    if (a instanceof ReflectionAnnotationMirror) {
      return ((ReflectionAnnotationMirror) a).getElementValues();
    } else {
      throw new IllegalArgumentException();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDocComment(Element e) {
    checkElement(e);
    return null; // As per the doc
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isDeprecated(Element e) {
    checkElement(e);
    return ((ReflectionElementImpl) e).getSource().isAnnotationPresent(java.lang.Deprecated.class);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Name getBinaryName(TypeElement type) {
    checkElement(type);
    return StringName.instance(((ReflectionTypeElementImpl) type).getSource().getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RuntimePackageElement getPackageOf(Element type) {
    checkElement(type);
    if (type instanceof RuntimePackageElement) {
      return (RuntimePackageElement) type;
    }

    Package p;
    if (type instanceof ReflectionTypeElementImpl) {
      p = ((ReflectionTypeElementImpl) type).getSource().getPackage();
    } else {
      ReflectionTypeElementImpl enclosingTypeElement = (ReflectionTypeElementImpl) getEnclosingTypeElement(
          (RuntimeElement) type);
      p = enclosingTypeElement.getSource().getPackage();
    }

    return CoreReflectionFactory.asMirror(p);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<? extends RuntimeElement> getAllMembers(TypeElement type) {
    checkElement(type);
    return getAllMembers((RuntimeTypeElement) type);
  }

  // Exercise for the reader: should this method, and similar
  // ones that specialize on the more specific argument types,
  // be addd to the public ReflectionElements API?
  public List<? extends RuntimeElement> getAllMembers(RuntimeTypeElement type) {
    return type.getAllMembers();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e) {
    checkElement(e);
    AnnotatedElement ae = ReflectionElementImpl.class.cast(e).getSource();
    Annotation[] annotations = ae.getAnnotations();
    int len = annotations.length;

    if (len > 0) {
      List<AnnotationMirror> res = new ArrayList<>(len);
      for (Annotation a : annotations) {
        res.add(CoreReflectionFactory.asMirror(a));
      }
      return Collections.unmodifiableList(res);
    } else {
      List<AnnotationMirror> ret = Collections.emptyList();
      return ret;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hides(Element hider, Element hidden) {
    checkElement(hider);
    checkElement(hidden);

    // Names must be equal
    if (!hider.getSimpleName().equals(hidden.getSimpleName())) {
      return false;
    }

    // Hides isn't reflexive
    if (hider.equals(hidden)) {
      return false;
    }

    // Hider and hidden needs to be field, method or type
    // and fields hide fields, types hide types, methods hide methods
    // IE a Field doesn't hide a Methods etc
    ElementKind hiderKind = hider.getKind();
    ElementKind hiddenKind = hidden.getKind();
    if (hiderKind.isField() && !hiddenKind.isField()) {
      return false;
    } else if (hiderKind.isClass() && !(hiddenKind.isClass() || hiddenKind.isInterface())) {
      return false;
    } else if (hiderKind.isInterface() && !(hiddenKind.isClass() || hiddenKind.isInterface())) {
      return false;
    } else if (hiderKind == ElementKind.METHOD && hiddenKind != ElementKind.METHOD) {
      return false;
    } else if (!(hiderKind.isClass() || hiderKind.isInterface() || hiderKind.isField()
        || hiderKind == ElementKind.METHOD)) {
      return false;
    }

    Set<Modifier> hm = hidden.getModifiers();
    // jls 8.4.8.2 only static methods can hide methods
    if (hider.getKind() == ElementKind.METHOD) {
      if (!hider.getModifiers().contains(Modifier.STATIC)) {
        return false; // hider not static
      } else if (!hm.contains(Modifier.STATIC)) { // we know it's a method
        return false; // hidden not static
      }

      // For methods we also need to check parameter types
      Class<?>[] h1 = ((ReflectionMethodElementImpl) hider).getSource().getParameterTypes();
      Class<?>[] h2 = ((ReflectionMethodElementImpl) hidden).getSource().getParameterTypes();
      if (h1.length != h2.length) {
        return false;
      }
      for (int i = 0; i < h1.length; i++) {
        if (h1[i] != h2[i]) {
          return false;
        }
      }
    }

    // You can only hide visible elements
    if (hm.contains(Modifier.PRIVATE)) {
      return false; // hidden private, can't be hidden
    } else if ((!(hm.contains(Modifier.PUBLIC) || hm.contains(Modifier.PROTECTED))) && // not
                                                                                       // private,
                                                                                       // not
                                                                                       // (public
                                                                                       // or
                                                                                       // protected)
                                                                                       // IE
                                                                                       // package
                                                                                       // private
        (!getPackageOf(hider).equals(getPackageOf(hidden)))) {
      return false; // hidden package private, and different packages, IE not visible
    }

    // Ok so now hider actually hides hidden if hider is
    // declared on a subtype of hidden.
    //
    // TODO: should this be a proper subtype or is that taken
    // care of by the reflexive check in the beginning?
    //
    TypeMirror hiderType = getEnclosingTypeElement((RuntimeElement) hider).asType();
    TypeMirror hiddenType = getEnclosingTypeElement((RuntimeElement) hidden).asType();

    return CoreReflectionFactory.getTypes().isSubtype(hiderType, hiddenType);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RuntimeTypeElement getEnclosingTypeElement(RuntimeElement e) {
    if (e.getKind() == ElementKind.PACKAGE) {
      return null;
    }

    if (e instanceof ReflectionTypeParameterElementImpl) {
      RuntimeElement encElem = ((ReflectionTypeParameterElementImpl) e).getEnclosingElement();
      if (encElem instanceof RuntimeTypeElement) {
        return (RuntimeTypeElement) encElem;
      } else {
        return getEnclosingTypeElement(encElem);
      }
    }

    Class<?> encl = null;
    if (e instanceof ReflectionTypeElementImpl) {
      encl = ((ReflectionTypeElementImpl) e).getSource().getDeclaringClass();
    } else if (e instanceof ReflectionExecutableElementImpl) {
      encl = (((ReflectionExecutableElementImpl) e).getSource()).getDeclaringClass();
    } else if (e instanceof ReflectionFieldElementImpl) {
      encl = ((ReflectionFieldElementImpl) e).getSource().getDeclaringClass();
    } else if (e instanceof ReflectionParameterElementImpl) {
      encl = ((ReflectionParameterElementImpl) e)
          .getSource()
          .getDeclaringExecutable()
          .getDeclaringClass();
    }

    return encl == null ? null : CoreReflectionFactory.asMirror(encl);
  }

  /**
   * {@inheritDoc}
   *
   * Note that this implementation does not handle the situation where A overrides
   * B and B overrides C but A does not directly override C. In this case, this
   * implementation will erroneously return false.
   */
  @Override
  public boolean overrides(
      ExecutableElement overrider,
      ExecutableElement overridden,
      TypeElement type) {
    checkElement(overrider);
    checkElement(overridden);
    checkElement(type);

    // TODO handle transitive overrides
    return overridesDirect(overrider, overridden, type);
  }

  private boolean overridesDirect(
      ExecutableElement overrider,
      ExecutableElement overridden,
      TypeElement type) {
    // Should we check that at least one of the types
    // overrider has is in fact a supertype of the TypeElement
    // 'type' supplied?

    ReflectionExecutableElementImpl rider = (ReflectionExecutableElementImpl) overrider;
    ReflectionExecutableElementImpl ridden = (ReflectionExecutableElementImpl) overridden;
    ReflectionTypeElementImpl riderType = (ReflectionTypeElementImpl) type;

    // Names must match, redundant - see subsignature below
    if (!rider.getSimpleName().equals(ridden.getSimpleName())) {
      return false;
    }

    // Constructors don't override
    // TODO: verify this fact
    if (rider.getKind() == ElementKind.CONSTRUCTOR || ridden.getKind() == ElementKind.CONSTRUCTOR) {
      return false;
    }

    // Overridden must be visible to be overridden
    // TODO Fix transitive visibility/override
    Set<Modifier> rm = ridden.getModifiers();
    if (rm.contains(Modifier.PRIVATE)) {
      return false; // overridden private, can't be overridden
    } else if ((!(rm.contains(Modifier.PUBLIC) || rm.contains(Modifier.PROTECTED))) && // not
                                                                                       // private,
                                                                                       // not
                                                                                       // (public
                                                                                       // or
                                                                                       // protected)
                                                                                       // IE
                                                                                       // package
                                                                                       // private
        (!getPackageOf(rider).equals(getPackageOf(ridden)))) {
      return false; // ridden package private, and different packages, IE not visible
    }

    // Static methods doesn't override
    if (rm.contains(Modifier.STATIC) || rider.getModifiers().contains(Modifier.STATIC)) {
      return false;
    }

    // Declaring class of overrider must be a subclass of declaring class of
    // overridden
    // except we use the parameter type as declaring class of overrider
    if (!CoreReflectionFactory
        .getTypes()
        .isSubtype(riderType.asType(), getEnclosingTypeElement(ridden).asType())) {
      return false;
    }

    // Now overrider overrides overridden if the signature of rider is a
    // subsignature of ridden
    return CoreReflectionFactory.getTypes().isSubsignature(rider.asType(), ridden.asType());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getConstantExpression(Object value) {
    return Constants.format(value);
  }

  // If CoreReflectionFactory were a proper part of the JDK, the
  // analogous functionality in javac could be reused.
  private static class Constants {
    /**
     * Returns a string representation of a constant value (given in standard
     * wrapped representation), quoted and formatted as in Java source.
     */
    public static String format(Object value) {
      if (value instanceof Byte)
        return formatByte((Byte) value);
      if (value instanceof Short)
        return formatShort((Short) value);
      if (value instanceof Long)
        return formatLong((Long) value);
      if (value instanceof Float)
        return formatFloat((Float) value);
      if (value instanceof Double)
        return formatDouble((Double) value);
      if (value instanceof Character)
        return formatChar((Character) value);
      if (value instanceof String)
        return formatString((String) value);
      if (value instanceof Integer || value instanceof Boolean)
        return value.toString();
      else
        throw new IllegalArgumentException(
            "Argument is not a primitive type or a string; it "
                + ((value == null) ? "is a null value." : "has class " + value.getClass().getName())
                + ".");
    }

    private static String formatByte(byte b) {
      return String.format("(byte)0x%02x", b);
    }

    private static String formatShort(short s) {
      return String.format("(short)%d", s);
    }

    private static String formatLong(long lng) {
      return lng + "L";
    }

    private static String formatFloat(float f) {
      if (Float.isNaN(f))
        return "0.0f/0.0f";
      else if (Float.isInfinite(f))
        return (f < 0) ? "-1.0f/0.0f" : "1.0f/0.0f";
      else
        return f + "f";
    }

    private static String formatDouble(double d) {
      if (Double.isNaN(d))
        return "0.0/0.0";
      else if (Double.isInfinite(d))
        return (d < 0) ? "-1.0/0.0" : "1.0/0.0";
      else
        return d + "";
    }

    private static String formatChar(char c) {
      return '\'' + quote(c) + '\'';
    }

    private static String formatString(String s) {
      return '"' + quote(s) + '"';
    }

    /**
     * Escapes each character in a string that has an escape sequence or is
     * non-printable ASCII. Leaves non-ASCII characters alone.
     */
    private static String quote(String s) {
      StringBuilder buf = new StringBuilder();
      for (int i = 0; i < s.length(); i++) {
        buf.append(quote(s.charAt(i)));
      }
      return buf.toString();
    }

    /**
     * Escapes a character if it has an escape sequence or is non-printable ASCII.
     * Leaves ASCII characters alone.
     */
    private static String quote(char ch) {
      switch (ch) {
      case '\b':
        return "\\b";
      case '\f':
        return "\\f";
      case '\n':
        return "\\n";
      case '\r':
        return "\\r";
      case '\t':
        return "\\t";
      case '\'':
        return "\\'";
      case '\"':
        return "\\\"";
      case '\\':
        return "\\\\";
      default:
        return (isPrintableAscii(ch)) ? String.valueOf(ch) : String.format("\\u%04x", (int) ch);
      }
    }

    /**
     * Is a character printable ASCII?
     */
    private static boolean isPrintableAscii(char ch) {
      return ch >= ' ' && ch <= '~';
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void printElements(Writer w, Element... elements) {
    ElementVisitor<?, ?> printer = getPrinter(w);
    try {
      for (Element e : elements) {
        checkElement(e);
        printer.visit(e);
      }
    } finally {
      try {
        w.flush();
      } catch (java.io.IOException e) {
        /* Ignore */;
      }
    }
  }

  private ElementVisitor<?, ?> getPrinter(Writer w) {
    // First try a reflective call into javac and if that
    // fails, fallback to a very simple toString-based
    // scanner.
    try {
      // reflective form of
      // return new
      // com.sun.tools.javac.processing.PrintingProcessor.PrintingElementVisitor(w,
      // getElements());
      Class<?> printProcClass = ClassLoader
          .getSystemClassLoader()
          .loadClass("com.sun.tools.javac.processing.PrintingProcessor$PrintingElementVisitor");
      Constructor<?> printProcCtor = printProcClass.getConstructor(Writer.class, Elements.class);
      return (ElementVisitor) printProcCtor.newInstance(w, CoreReflectionFactory.getElements());
    } catch (ReflectiveOperationException | SecurityException e) {
      return new ElementScanner9<Writer, Void>(w) {
        @Override
        public Writer scan(Element e, Void v) {
          try {
            DEFAULT_VALUE.append(e.toString());
            DEFAULT_VALUE.append("\n");
          } catch (java.io.IOException ioe) {
            throw new RuntimeException(ioe);
          }
          return DEFAULT_VALUE;
        }
      };
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Name getName(CharSequence cs) {
    return StringName.instance(cs.toString());
  }

  private void checkElement(Element e) {
    if (!(e instanceof ReflectionElementImpl)) {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public boolean isFunctionalInterface(TypeElement e) {
    throw new UnsupportedOperationException();
    // Update once this functionality is in core reflection
  }
}
package uk.co.strangeskies.reflection.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

/**
 * This class provides a proof-of-concept implementation of the {@code
 * javax.lang.model.*} API backed by core reflection. That is, rather than
 * having a source file or compile-time class file as the originator of the
 * information about an element or type, as done during standard annotation
 * processing, runtime core reflection objects serve that purpose instead.
 *
 * With this kind of implementation, the same logic can be used for both
 * compile-time and runtime processing of annotations.
 *
 * The nested types in this class define a specialization of {@code
 * javax.lang.model.*} to provide some additional functionality and type
 * information. The original {@code javax.lang.model.*} API was designed to
 * accommodate such a specialization by using wildcards in the return types of
 * methods.
 *
 * It would be technically possible for further specializations of the API
 * implemented in this class to define alternative semantics of annotation
 * look-up. For example to allow one annotation to have the effect of
 * macro-expanding into a set of other annotations.
 *
 * Some aspects of the implementation are left as "exercises for the reader" to
 * complete if interested.
 *
 * When passed null pointers, the methods defined in this type will generally
 * throw null pointer exceptions.
 *
 * To get started, first compile this file with a command line like:
 *
 * <pre>
 * $JDK/bin/javac -parameters -Xdoclint:all/public -Xlint:all -d $OUTPUT_DIR CoreReflectionFactory.java
 * </pre>
 *
 * and then run the main method of {@code CoreReflectionFactory}, which will
 * print out a representation of {@code
 * CoreReflectionFactory}. To use the printing logic defined in {@code
 * javac}, put {@code tools.jar} on the classpath as in:
 *
 * <pre>
 * $JDK/bin/java -cp $OUTPUT_DIR:$JDK_ROOT/lib/tools.jar CoreReflectionFactory
 * </pre>
 *
 * @author Joseph D. Darcy (darcy)
 * @author Joel Borggren-Franck (jfranck)
 */
public class CoreReflectionFactory {
  private CoreReflectionFactory() {
    throw new AssertionError("No instances of CoreReflectionFactory for you!");
  }

  /**
   * Returns a reflection type element mirroring a {@code Class} object.
   * 
   * @return a reflection type element mirroring a {@code Class} object
   * @param clazz
   *          the {@code Class} to mirror
   */
  public static ReflectionTypeElement createMirror(Class<?> clazz) {
    return new ReflectionTypeElementImpl(Objects.requireNonNull(clazz));
  }

  /**
   * Returns a reflection package element mirroring a {@code Package} object.
   * 
   * @return a reflection package element mirroring a {@code Package} object
   * @param pkg
   *          the {@code Package} to mirror
   */
  public static ReflectionPackageElement createMirror(Package pkg) {
    // Treat a null pkg to mean an unnamed package.
    return new ReflectionPackageElementImpl(pkg);
  }

  /**
   * Returns a reflection variable element mirroring a {@code Field} object.
   * 
   * @return a reflection variable element mirroring a {@code Field} object
   * @param field
   *          the {@code Field} to mirror
   */
  public static ReflectionVariableElement createMirror(Field field) {
    return new ReflectionFieldElementImpl(Objects.requireNonNull(field));
  }

  /**
   * Returns a reflection executable element mirroring a {@code Method} object.
   * 
   * @return a reflection executable element mirroring a {@code Method} object
   * @param method
   *          the {@code Method} to mirror
   */
  public static ReflectionExecutableElement createMirror(Method method) {
    return new ReflectionMethodElementImpl(Objects.requireNonNull(method));
  }

  /**
   * Returns a reflection executable element mirroring a {@code Constructor}
   * object.
   * 
   * @return a reflection executable element mirroring a {@code Constructor}
   *         object
   * @param constructor
   *          the {@code Constructor} to mirror
   */
  public static ReflectionExecutableElement createMirror(Constructor<?> constructor) {
    return new ReflectionConstructorExecutableElementImpl(Objects.requireNonNull(constructor));
  }

  /**
   * Returns a type parameter element mirroring a {@code TypeVariable} object.
   * 
   * @return a type parameter element mirroring a {@code TypeVariable} object
   * @param tv
   *          the {@code TypeVariable} to mirror
   */
  public static TypeParameterElement createMirror(java.lang.reflect.TypeVariable<?> tv) {
    return new ReflectionTypeParameterElementImpl(Objects.requireNonNull(tv));
  }

  /**
   * Returns a variable element mirroring a {@code Parameter} object.
   * 
   * @return a variable element mirroring a {@code Parameter} object
   * @param p
   *          the {Parameter} to mirror
   */
  public static VariableElement createMirror(java.lang.reflect.Parameter p) {
    return new ReflectionParameterElementImpl(Objects.requireNonNull(p));
  }

  /**
   * Returns an annotation mirror mirroring an annotation object.
   * 
   * @return an annotation mirror mirroring an annotation object
   * @param annotation
   *          the annotation to mirror
   */
  public static AnnotationMirror createMirror(Annotation annotation) {
    return new ReflectionAnnotationMirror(Objects.requireNonNull(annotation));
  }

  /**
   * Returns a {@code Types} utility object for type objects backed by core
   * reflection.
   * 
   * @return a {@code Types} utility object for type objects backed by core
   *         reflection
   */
  public static javax.lang.model.util.Types getTypes() {
    return ReflectionTypes.instance();
  }

  /**
   * Returns an {@code Elements} utility object for type objects backed by core
   * reflection.
   * 
   * @return an {@code Elements} utility object for type objects backed by core
   *         reflection
   */
  public static Elements getElements() {
    return ReflectionElementsImpl.instance();
  }

  // Helper
  static TypeMirror createTypeMirror(Class<?> c) {
    return TypeFactory.instance(Objects.requireNonNull(c));
  }
}

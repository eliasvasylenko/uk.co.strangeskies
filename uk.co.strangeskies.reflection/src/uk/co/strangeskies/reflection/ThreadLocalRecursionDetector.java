package uk.co.strangeskies.reflection;

import static java.lang.ThreadLocal.withInitial;

import java.util.ArrayList;
import java.util.List;

public class ThreadLocalRecursionDetector {
  private static final ThreadLocal<ThreadLocalRecursionDetector> THREAD_LOCAL = withInitial(
      ThreadLocalRecursionDetector::new);

  private final List<Object> stack = new ArrayList<>();

  private ThreadLocalRecursionDetector() {}

  public static void push(Object type) {
    THREAD_LOCAL.get().stack.add(type);
  }

  public static long repeatCount(Object type) {
    return THREAD_LOCAL.get().stack.stream().filter(type::equals).count();
  }

  public static void pop() {
    ThreadLocalRecursionDetector detector = THREAD_LOCAL.get();

    detector.stack.remove(detector.stack.size() - 1);
    if (detector.stack.isEmpty()) {
      THREAD_LOCAL.remove();
    }
  }
}
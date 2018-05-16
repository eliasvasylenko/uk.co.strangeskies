package uk.co.strangeskies.text.properties;

import java.lang.reflect.AnnotatedType;
import java.util.List;

public interface PropertyValueLoader {
  Object load(AnnotatedType valueType, String key, String valueString, List<?> arguments);

  Object getDefault(String key, List<?> arguments);

  boolean providesDefault();
}

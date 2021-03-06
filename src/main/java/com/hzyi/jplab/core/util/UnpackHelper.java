package com.hzyi.jplab.core.util;

import com.hzyi.jplab.core.application.exceptions.IllegalPropertyTypeException;
import com.hzyi.jplab.core.application.exceptions.IllegalPropertyValueException;
import com.hzyi.jplab.core.application.exceptions.MissingRequiredPropertyException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import lombok.AllArgsConstructor;
import lombok.Getter;

/* A helper class that helps pop entries in a map into a builder object. */
@AllArgsConstructor
public class UnpackHelper<BuilderT> {

  @Getter private final BuilderT builder;
  private final Map<String, ?> source;
  private final Class<?> destinationType;

  /**
   * Used by `unpack`. Caller will call `test` on certain values and if it evaluates to false,
   * caller will call `getException` to create a Throwable and throw it.
   */
  public interface ThrowingPredicate<PropertyT, ThrowableT extends RuntimeException> {
    boolean test(PropertyT value);

    ThrowableT getException(
        String property, PropertyT value, Class<PropertyT> expectedType, Class<?> destinationType);
  }

  /** An interface for collecting two values from the map. */
  @FunctionalInterface
  public interface BiCollector<BuilderT, PropertyT1, PropertyT2> {
    public BuilderT apply(BuilderT builder, PropertyT1 p1, PropertyT2 p2);
  }

  /** Creates an UnpackHelper. */
  public static <BuilderT> UnpackHelper<BuilderT> of(
      BuilderT builder, Map<String, ?> source, Class<?> destinationType) {
    return new UnpackHelper(builder, source, destinationType);
  }

  /** Unpacks a property from a map and return it. */
  public <PropertyT> PropertyT unpack(
      String property, Class<PropertyT> expectedType, ThrowingPredicate<PropertyT, ?>... checkers) {
    return unpack(property, expectedType, Arrays.asList(checkers));
  }

  /** Unpacks a property from a map and collects it into the target object. */
  public <PropertyT> PropertyT unpack(
      String property,
      Class<PropertyT> expectedType,
      List<ThrowingPredicate<PropertyT, ?>> checkers) {
    Object value = source.get(property);
    if (value != null && !expectedType.isInstance(value)) {
      throw new IllegalPropertyTypeException(
          property, value.getClass().getName(), expectedType.getName());
    }
    PropertyT valueT = (PropertyT) value;
    for (ThrowingPredicate<PropertyT, ?> checker : checkers) {
      if (!checker.test(valueT)) {
        throw checker.getException(property, valueT, expectedType, destinationType);
      }
    }
    return (PropertyT) value;
  }

  public Double unpack(String property, ThrowingPredicate<Double, ?>... checkers) {
    return unpack(property, Arrays.asList(checkers));
  }

  public Double unpack(String property, List<ThrowingPredicate<Double, ?>> checkers) {
    Object value = source.get(property);
    if (value != null && !(value instanceof Double)) {
      throw new IllegalPropertyTypeException(
          property, value.getClass().getName(), Double.class.getName());
    }
    Double valueT = (Double) value;
    for (ThrowingPredicate<Double, ?> checker : checkers) {
      if (!checker.test(valueT)) {
        throw checker.getException(property, valueT, Double.class, destinationType);
      }
    }
    return (Double) value;
  }

  /** Unpacks a property from a map and collects it into the target object. */
  public <PropertyT> UnpackHelper<BuilderT> unpack(
      String property,
      Class<PropertyT> expectedType,
      BiFunction<BuilderT, PropertyT, BuilderT> collector,
      ThrowingPredicate<PropertyT, ?>... checkers) {
    PropertyT value = unpack(property, expectedType, checkers);
    if (value != null) {
      collector.apply(builder, value);
    }
    return this;
  }

  /** Unpacks a property from a map and collects it into the target object. */
  public UnpackHelper<BuilderT> unpack(
      String property,
      BiFunction<BuilderT, Double, BuilderT> collector,
      ThrowingPredicate<Double, ?>... checkers) {
    Double value = unpack(property, checkers);
    if (value != null) {
      collector.apply(builder, value);
    }
    return this;
  }

  /** Unpacks two properties from a map and collects them into the target object. */
  public <PropertyT1, PropertyT2> UnpackHelper<BuilderT> unpack(
      String p1,
      Class<PropertyT1> type1,
      String p2,
      Class<PropertyT2> type2,
      BiCollector<BuilderT, PropertyT1, PropertyT2> collector) {

    return unpack(
        p1, type1, p2, type2, collector, Collections.emptyList(), Collections.emptyList());
  }

  /** Unpacks two properties from a map and collects them into the target object. */
  public <PropertyT1, PropertyT2> UnpackHelper<BuilderT> unpack(
      String p1,
      Class<PropertyT1> type1,
      String p2,
      Class<PropertyT2> type2,
      BiCollector<BuilderT, PropertyT1, PropertyT2> collector,
      List<ThrowingPredicate<PropertyT1, ?>> checkers1,
      List<ThrowingPredicate<PropertyT2, ?>> checkers2) {

    PropertyT1 value1 = unpack(p1, type1, checkers1);
    PropertyT2 value2 = unpack(p2, type2, checkers2);
    collector.apply(builder, value1, value2);
    return this;
  }

  /** Checks if input is non-null. Provides a MissingRequiredPropertyException otherwise. */
  public static <PropertyT>
      ThrowingPredicate<PropertyT, MissingRequiredPropertyException> checkExistence() {
    return new ThrowingPredicate<PropertyT, MissingRequiredPropertyException>() {
      @Override
      public boolean test(PropertyT value) {
        return value != null;
      }

      @Override
      public MissingRequiredPropertyException getException(
          String property,
          PropertyT value,
          Class<PropertyT> expectedType,
          Class<?> destinationType) {
        return new MissingRequiredPropertyException(destinationType.getName() + "." + property);
      }
    };
  }

  /**
   * Checks if input is a number and is greater than zero. Provides an IllegalPropertyValueException
   * otherwise.
   */
  public static <PropertyT>
      ThrowingPredicate<PropertyT, IllegalPropertyValueException> checkPositivity() {
    return new ThrowingPredicate<PropertyT, IllegalPropertyValueException>() {
      @Override
      public boolean test(PropertyT value) {
        return value == null
            || (value instanceof Integer && (Integer) value > 0)
            || (value instanceof Double && (Double) value > 0.0);
      }

      @Override
      public IllegalPropertyValueException getException(
          String property,
          PropertyT value,
          Class<PropertyT> expectedType,
          Class<?> destinationType) {
        if (!(value instanceof Number)) {
          return new IllegalPropertyValueException(
              destinationType.getName() + "." + property,
              String.format("not a number, type: %s", value.getClass()));
        }
        return new IllegalPropertyValueException(
            destinationType.getName() + "." + property, "must be positive");
      }
    };
  }
}

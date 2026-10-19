package com.nbugaenco.softdesign.builder.model;

import java.util.List;

/**
 * Validation helpers of the builders.
 */
final class Validation {

  private static final StackWalker WALKER =
      StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

  private Validation() {
  }

  static String text(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
    return value;
  }

  static int range(int value, int min, int max, String field) {
    if (value < min || value > max) {
      throw new IllegalArgumentException(
          field + " must be in [" + min + ", " + max + "], but was " + value);
    }
    return value;
  }

  /**
   * @throws IllegalStateException with all errors, if the list is not empty
   */
  static void throwIfAny(String object, List<String> errors) {
    if (!errors.isEmpty()) {
      throw new IllegalStateException("Invalid " + object + ":\n" + String.join("\n", errors));
    }
  }

  /**
   * Returns {@code File.java:line} of the first caller outside this package.
   */
  static String callSite() {
    return WALKER.walk(frames -> frames
        .filter(f -> f.getDeclaringClass().getPackage() != Validation.class.getPackage())
        .findFirst()
        .map(f -> f.getFileName() + ":" + f.getLineNumber())
        .orElse("unknown"));
  }
}

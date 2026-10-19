package com.nbugaenco.softdesign.pipeline.core;

/**
 * One action of a {@link Pipeline} on a context of type {@code C}.
 */
public interface Step<C> {

  void execute(C context);

  /**
   * Appends a description of this step. The default uses {@code toString()}, so a record step
   * describes itself with its fields.
   */
  default void describe(StringBuilder out) {
    out.append(this);
  }

  default String describe() {
    StringBuilder out = new StringBuilder();
    describe(out);
    return out.toString();
  }
}

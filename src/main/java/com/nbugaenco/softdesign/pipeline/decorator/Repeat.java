package com.nbugaenco.softdesign.pipeline.decorator;

import com.nbugaenco.softdesign.pipeline.core.Step;

/**
 * Decorator: runs the step {@code times} times, for example a double shot.
 */
public record Repeat<C>(int times, Step<C> step) implements Step<C> {

  @Override
  public void execute(C context) {
    for (int i = 0; i < times; i++) {
      step.execute(context);
    }
  }

  @Override
  public void describe(StringBuilder out) {
    out.append(times).append("x ");
    step.describe(out);
  }
}

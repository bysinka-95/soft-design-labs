package com.nbugaenco.softdesign.pipeline.decorator;

import com.nbugaenco.softdesign.pipeline.core.Step;

/**
 * Decorator: prints the step before it runs and the context after it.
 */
public record Logged<C>(Step<C> step) implements Step<C> {

  @Override
  public void execute(C context) {
    System.out.println("> " + step.describe());
    step.execute(context);
    System.out.println("  " + context);
  }

  @Override
  public void describe(StringBuilder out) {
    out.append("Logged(");
    step.describe(out);
    out.append(')');
  }
}

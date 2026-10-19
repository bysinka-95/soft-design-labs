package com.nbugaenco.softdesign.pipeline.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

/**
 * An ordered list of steps. A pipeline is also a step, so one pipeline can contain another. Methods
 * that find steps by type match subclasses too ({@link Class#isInstance}).
 */
public final class Pipeline<C extends Context> implements Step<C> {

  private final List<Step<C>> steps;

  @SafeVarargs
  public Pipeline(Step<C>... steps) {
    this.steps = new ArrayList<>(List.of(steps));
  }

  public List<Step<C>> steps() {
    return steps;
  }

  /**
   * Runs the steps in order. Stops when a step calls {@link Context#stop()}.
   */
  @Override
  public void execute(C context) {
    for (Step<C> step : steps) {
      if (context.isDone()) {
        return;
      }
      step.execute(context);
    }
  }

  @Override
  public void describe(StringBuilder out) {
    out.append("Pipeline[");
    for (int i = 0; i < steps.size(); i++) {
      if (i > 0) {
        out.append(", ");
      }
      steps.get(i).describe(out);
    }
    out.append(']');
  }

  /**
   * Prints each step on its own line, with its index.
   */
  public void print() {
    for (int i = 0; i < steps.size(); i++) {
      System.out.println(i + ": " + steps.get(i).describe());
    }
  }

  /**
   * Replaces the first step of the type. Returns {@code false} if no step has this type.
   */
  public boolean replaceFirst(Class<?> type, Step<C> step) {
    int i = indexOf(type);
    if (i >= 0) {
      steps.set(i, step);
    }
    return i >= 0;
  }

  public void replaceAll(Class<?> type, Step<C> step) {
    wrapAll(type, ignore -> step);
  }

  /**
   * Replaces each step of the type with {@code wrap(step)}, for example {@code Logged::new}.
   */
  public void wrapAll(Class<?> type, UnaryOperator<Step<C>> wrap) {
    steps.replaceAll(s -> type.isInstance(s) ? wrap.apply(s) : s);
  }

  /**
   * Moves the first step of the type to the index. Returns {@code false} if no step has this type.
   */
  public boolean moveTo(Class<?> type, int index) {
    int i = indexOf(type);
    if (i >= 0) {
      steps.add(index, steps.remove(i));
    }
    return i >= 0;
  }

  private int indexOf(Class<?> type) {
    return IntStream.range(0, steps.size())
        .filter(i -> type.isInstance(steps.get(i)))
        .findFirst()
        .orElse(-1);
  }
}

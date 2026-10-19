package com.nbugaenco.softdesign.pipeline.core;

/**
 * Mutable state that the steps of a {@link Pipeline} share. A step calls {@link #stop()} to stop
 * the steps after it (responsibility chain). A stopped context cannot start again.
 */
public abstract class Context {

  private boolean done;

  public boolean isDone() {
    return done;
  }

  public void stop() {
    done = true;
  }
}

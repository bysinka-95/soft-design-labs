package com.nbugaenco.softdesign.pipeline.order.step;

import com.nbugaenco.softdesign.pipeline.core.Step;
import com.nbugaenco.softdesign.pipeline.order.Order;

/**
 * Prints the receipt. The step has no state, so one instance is enough (enum singleton).
 */
public enum Receipt implements Step<Order> {
  INSTANCE;

  @Override
  public void execute(Order order) {
    System.out.printf("Receipt: %s %s, price %d, change %d%n",
        order.drink, order.extras, order.price, order.paid - order.price);
  }

  @Override
  public String toString() {
    return "Receipt";
  }
}

package com.nbugaenco.softdesign.pipeline.order.step;

import com.nbugaenco.softdesign.pipeline.core.Step;
import com.nbugaenco.softdesign.pipeline.order.Order;

/**
 * Takes the money. Stops the pipeline if the money is less than the price.
 */
public record Pay(int amount) implements Step<Order> {

  @Override
  public void execute(Order order) {
    order.paid = amount;
    if (amount < order.price) {
      System.out.println("Not enough money: " + amount + " < " + order.price);
      order.stop();
    }
  }
}

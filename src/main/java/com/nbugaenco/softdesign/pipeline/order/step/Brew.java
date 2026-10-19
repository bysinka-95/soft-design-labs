package com.nbugaenco.softdesign.pipeline.order.step;

import com.nbugaenco.softdesign.pipeline.core.Step;
import com.nbugaenco.softdesign.pipeline.order.Order;

public record Brew(String drink, int price) implements Step<Order> {

  @Override
  public void execute(Order order) {
    order.drink = drink;
    order.price += price;
  }
}

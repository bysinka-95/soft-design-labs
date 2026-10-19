package com.nbugaenco.softdesign.pipeline.order.step;

import com.nbugaenco.softdesign.pipeline.core.Step;
import com.nbugaenco.softdesign.pipeline.order.Order;

public record AddExtra(String extra, int price) implements Step<Order> {

  @Override
  public void execute(Order order) {
    order.extras.add(extra);
    order.price += price;
  }
}

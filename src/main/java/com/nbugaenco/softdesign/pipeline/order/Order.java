package com.nbugaenco.softdesign.pipeline.order;

import com.nbugaenco.softdesign.pipeline.core.Context;
import java.util.ArrayList;
import java.util.List;

/**
 * A coffee order: the first context type. Prices are in MDL.
 */
public final class Order extends Context {

  public final List<String> extras = new ArrayList<>();
  public String drink = "";
  public int price;
  public int paid;

  @Override
  public String toString() {
    return "Order[drink=%s, extras=%s, price=%d, paid=%d]".formatted(drink, extras, price, paid);
  }
}

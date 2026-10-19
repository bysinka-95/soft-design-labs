package com.nbugaenco.softdesign.pipeline.stock;

import com.nbugaenco.softdesign.pipeline.core.Context;
import java.util.Map;
import java.util.TreeMap;

/**
 * The stock of a coffee shop: the second context type.
 */
public final class Stock extends Context {

  public final Map<String, Integer> items = new TreeMap<>();

  @Override
  public String toString() {
    return "Stock" + items;
  }
}

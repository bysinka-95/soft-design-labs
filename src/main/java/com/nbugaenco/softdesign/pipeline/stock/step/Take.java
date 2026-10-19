package com.nbugaenco.softdesign.pipeline.stock.step;

import com.nbugaenco.softdesign.pipeline.core.Step;
import com.nbugaenco.softdesign.pipeline.stock.Stock;

/**
 * Takes items from the stock. Stops the pipeline if the stock has too few items.
 */
public record Take(String item, int count) implements Step<Stock> {

  @Override
  public void execute(Stock stock) {
    int left = stock.items.getOrDefault(item, 0) - count;
    if (left < 0) {
      System.out.println("Out of " + item);
      stock.stop();
    } else {
      stock.items.put(item, left);
    }
  }
}

package com.nbugaenco.softdesign.pipeline.stock.step;

import com.nbugaenco.softdesign.pipeline.core.Step;
import com.nbugaenco.softdesign.pipeline.stock.Stock;

public record Restock(String item, int count) implements Step<Stock> {

  @Override
  public void execute(Stock stock) {
    stock.items.merge(item, count, Integer::sum);
  }
}

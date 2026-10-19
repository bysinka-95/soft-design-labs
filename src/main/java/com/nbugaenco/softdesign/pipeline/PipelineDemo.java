package com.nbugaenco.softdesign.pipeline;

import com.nbugaenco.softdesign.pipeline.core.Pipeline;
import com.nbugaenco.softdesign.pipeline.decorator.Logged;
import com.nbugaenco.softdesign.pipeline.decorator.Repeat;
import com.nbugaenco.softdesign.pipeline.order.Order;
import com.nbugaenco.softdesign.pipeline.order.step.AddExtra;
import com.nbugaenco.softdesign.pipeline.order.step.Brew;
import com.nbugaenco.softdesign.pipeline.order.step.Pay;
import com.nbugaenco.softdesign.pipeline.order.step.Receipt;
import com.nbugaenco.softdesign.pipeline.stock.Stock;
import com.nbugaenco.softdesign.pipeline.stock.step.Restock;
import com.nbugaenco.softdesign.pipeline.stock.step.Take;

/**
 * Demo of the lab. Run:
 * {@code java src/main/java/com/nbugaenco/softdesign/pipeline/PipelineDemo.java}
 */
class PipelineDemo {

  static void main() {
    Pipeline<Order> orderPipeline = new Pipeline<>(
        new Brew("latte", 40),
        new Repeat<>(2, new AddExtra("syrup", 5)),
        new AddExtra("oat milk", 10),
        new Pay(100),
        Receipt.INSTANCE);
    orderPipeline.print();
    orderPipeline.execute(new Order());

    System.out.println("\n-- espresso, logged extras, too little money");
    orderPipeline.replaceFirst(Brew.class, new Brew("espresso", 30));
    orderPipeline.replaceAll(Pay.class, new Pay(20));
    orderPipeline.wrapAll(AddExtra.class, Logged::new);
    System.out.println(orderPipeline.describe());
    orderPipeline.execute(new Order());

    System.out.println("\n-- stock: nested pipeline, stops when beans run out");
    Pipeline<Stock> morningStockPipeline = new Pipeline<>(new Restock("beans", 3),
        new Restock("cups", 10));
    Pipeline<Stock> stockPipeline = new Pipeline<>(
        morningStockPipeline,
        new Take("beans", 2),
        new Take("beans", 2),
        new Take("cups", 1));
    stockPipeline.wrapAll(Take.class, Logged::new);
    stockPipeline.print();
    stockPipeline.execute(new Stock());
  }
}

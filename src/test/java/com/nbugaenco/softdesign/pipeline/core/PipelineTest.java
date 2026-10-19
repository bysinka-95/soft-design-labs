package com.nbugaenco.softdesign.pipeline.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.nbugaenco.softdesign.pipeline.decorator.Logged;
import com.nbugaenco.softdesign.pipeline.decorator.Repeat;
import com.nbugaenco.softdesign.pipeline.order.step.AddExtra;
import com.nbugaenco.softdesign.pipeline.order.step.Brew;
import com.nbugaenco.softdesign.pipeline.order.Order;
import com.nbugaenco.softdesign.pipeline.order.step.Pay;
import com.nbugaenco.softdesign.pipeline.order.step.Receipt;
import com.nbugaenco.softdesign.pipeline.stock.step.Restock;
import com.nbugaenco.softdesign.pipeline.stock.Stock;
import com.nbugaenco.softdesign.pipeline.stock.step.Take;
import org.junit.jupiter.api.Test;

class PipelineTest {

  private final Pipeline<Order> latte = new Pipeline<>(
      new Brew("latte", 40),
      new Repeat<>(2, new AddExtra("syrup", 5)),
      new Pay(100),
      Receipt.INSTANCE);

  @Test
  void executesStepsInOrder() {
    Order order = new Order();
    latte.execute(order);

    assertThat(order.drink).isEqualTo("latte");
    assertThat(order.extras).containsExactly("syrup", "syrup");
    assertThat(order.price).isEqualTo(50);
    assertThat(order.isDone()).isFalse();
  }

  @Test
  void stopsWhenStepSetsDone() {
    Order order = new Order();
    new Pipeline<>(new Brew("latte", 40), new Pay(10), new AddExtra("syrup", 5)).execute(order);

    assertThat(order.isDone()).isTrue();
    assertThat(order.extras).isEmpty();
  }

  @Test
  void nestedPipelineStopsOuterPipeline() {
    Stock stock = new Stock();
    new Pipeline<>(
        new Pipeline<>(new Restock("beans", 3), new Take("beans", 5)),
        new Restock("cups", 1)).execute(stock);

    assertThat(stock.isDone()).isTrue();
    assertThat(stock.items).containsOnlyKeys("beans");
  }

  @Test
  void describesSteps() {
    assertThat(latte.describe()).isEqualTo(
        "Pipeline[Brew[drink=latte, price=40], 2x AddExtra[extra=syrup, price=5], "
            + "Pay[amount=100], Receipt]");
  }

  @Test
  void replacesWrapsAndMovesSteps() {
    assertThat(latte.replaceFirst(Brew.class, new Brew("mocha", 45))).isTrue();
    latte.replaceAll(Pay.class, new Pay(1));
    latte.wrapAll(Repeat.class, Logged::new);
    assertThat(latte.moveTo(Receipt.class, 0)).isTrue();
    assertThat(latte.moveTo(Take.class, 0)).isFalse();

    assertThat(latte.steps()).containsExactly(
        Receipt.INSTANCE,
        new Brew("mocha", 45),
        new Logged<>(new Repeat<>(2, new AddExtra("syrup", 5))),
        new Pay(1));
  }
}

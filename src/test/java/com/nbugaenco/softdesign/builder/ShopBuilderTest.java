package com.nbugaenco.softdesign.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nbugaenco.softdesign.builder.model.Barista;
import com.nbugaenco.softdesign.builder.model.CoffeeShop;
import com.nbugaenco.softdesign.builder.model.Drink;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

// The test is outside the model package: the builders report the lines of their callers.
class ShopBuilderTest {

  private final Drink espresso = Drink.builder().name("espresso").price(30).volumeMl(40).build();

  private final CoffeeShop shop = CoffeeShop.builder()
      .name("Bean There")
      .drink(espresso)
      .drink(d -> d.name("latte").price(45))
      .scope(s -> s
          .defaults(b -> b.experienceYears(1).signature("latte"))
          .barista(b -> b.name("Ion"))
          .barista(b -> b.name("Maria").signature(espresso)))
      .build();

  @Test
  void buildsShop() throws IOException {
    List<Object> lines = new ArrayList<>();
    lines.add(shop.name());
    lines.addAll(shop.menu());
    lines.addAll(shop.baristas());

    Snapshot.assertMatches("shop",
        lines.stream().map(String::valueOf).collect(Collectors.joining("\n")));
  }

  @Test
  void scopeAppliesDefaultsAndConfigChangesThem() {
    Drink latte = shop.menu().getLast();

    assertThat(shop.baristas()).containsExactly(
        new Barista("Ion", 1, latte),
        new Barista("Maria", 1, espresso));
  }

  @Test
  void toBuilderMakesEqualCopyThatCanChange() {
    assertThat(shop.toBuilder().build()).isEqualTo(shop);

    CoffeeShop renamed = shop.toBuilder().name("Bean Here").build();
    assertThat(renamed.name()).isEqualTo("Bean Here");
    assertThat(shop.name()).isEqualTo("Bean There");
  }

  @Test
  void setterRejectsInvalidValue() {
    assertThatThrownBy(() -> Drink.builder().volumeMl(5))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("volumeMl must be in [30, 1000], but was 5");
  }

  @Test
  void childBuilderChecksRequiredFields() {
    assertThatThrownBy(() -> Barista.builder().signature("latte").build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("barista name is not set")
        .hasMessageContaining("unknown drink 'latte'");
  }

  @Test
  void buildReportsAllErrorsWithLines() throws IOException {
    CoffeeShop.Builder builder = CoffeeShop.builder()
        .drink(d -> d.name("latte").price(45))
        .drink(d -> d.name("latte"))
        .drink(d -> d.price(10))
        .barista(b -> b.signature("mocha"));

    assertThatThrownBy(builder::build)
        .isInstanceOf(IllegalStateException.class)
        .satisfies(e -> Snapshot.assertMatches("errors", e.getMessage()));
  }

  @Test
  void builtModelCannotChange() {
    assertThatThrownBy(() -> shop.menu().add(espresso))
        .isInstanceOf(UnsupportedOperationException.class);
  }
}

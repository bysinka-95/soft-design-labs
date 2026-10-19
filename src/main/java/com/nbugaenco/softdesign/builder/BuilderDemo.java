package com.nbugaenco.softdesign.builder;

import com.nbugaenco.softdesign.builder.model.CoffeeShop;
import com.nbugaenco.softdesign.builder.model.Drink;

/**
 * Demo of the lab. Run: {@code java src/main/java/com/nbugaenco/softdesign/builder/BuilderDemo.java}
 */
class BuilderDemo {

  static void main() {
    Drink espresso = Drink.builder().name("espresso").price(30).volumeMl(40).build();

    CoffeeShop shop = CoffeeShop.builder()
        .name("Bean There")
        .drink(espresso)
        .drink(d -> d.name("latte").price(45))
        .drink(d -> d.name("flat white").price(50).volumeMl(180))
        .barista(b -> b.name("Ana").experienceYears(5).signature("flat white"))
        .scope(s -> s
            .defaults(b -> b.experienceYears(1).signature("latte"))
            .barista(b -> b.name("Ion"))
            .barista(b -> b.name("Maria").signature(espresso)))
        .build();
    print(shop);

    System.out.println("\n-- toBuilder: a changed copy, the original stays the same");
    CoffeeShop bigger = shop.toBuilder()
        .drink(d -> d.name("mocha").price(55))
        .barista(b -> b.name("Dan").signature("mocha"))
        .build();
    print(bigger);
    System.out.println("Original menu size: " + shop.menu().size());

    System.out.println("\n-- setter validation fails at once");
    try {
      Drink.builder().price(-5);
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }

    System.out.println("\n-- build validation reports all objects");
    try {
      CoffeeShop.builder()
          .drink(d -> d.name("latte").price(45))
          .drink(d -> d.name("latte"))
          .drink(d -> d.price(10))
          .barista(b -> b.signature("mocha"))
          .build();
    } catch (IllegalStateException e) {
      System.out.println(e.getMessage());
    }
  }

  private static void print(CoffeeShop shop) {
    System.out.println(shop.name());
    shop.menu().forEach(System.out::println);
    shop.baristas().forEach(System.out::println);
  }
}

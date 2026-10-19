package com.nbugaenco.softdesign.builder.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A coffee shop with its menu and baristas. The lists are copies that cannot change.
 */
public record CoffeeShop(String name, List<Drink> menu, List<Barista> baristas) {

  public CoffeeShop {
    menu = List.copyOf(menu);
    baristas = List.copyOf(baristas);
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    Builder builder = builder().name(name);
    for (Drink drink : menu) {
      builder.drink(drink);
    }
    for (Barista barista : baristas) {
      builder.barista(barista);
    }
    return builder;
  }

  /**
   * Builds the shop with all its objects. The child builders stay mutable until {@link #build()},
   * which checks all of them together.
   */
  public static final class Builder {

    private final List<Drink.Builder> drinks = new ArrayList<>();
    private final List<Barista.Builder> baristas = new ArrayList<>();
    private String name;

    private Builder() {
    }

    public Builder name(String name) {
      this.name = Validation.text(name, "name");
      return this;
    }

    public Builder drink(Drink drink) {
      drinks.add(drink.toBuilder());
      return this;
    }

    /**
     * Adds a drink and gives its builder to {@code config} (configuration delegate).
     */
    public Builder drink(Consumer<Drink.Builder> config) {
      Drink.Builder drink = Drink.builder();
      config.accept(drink);
      drinks.add(drink);
      return this;
    }

    public Builder barista(Barista barista) {
      baristas.add(barista.toBuilder());
      return this;
    }

    public Builder barista(Consumer<Barista.Builder> config) {
      Barista.Builder barista = Barista.builder();
      config.accept(barista);
      baristas.add(barista);
      return this;
    }

    /**
     * Adds baristas with common settings, see {@link BaristaScope}.
     */
    public Builder scope(Consumer<BaristaScope> config) {
      config.accept(new BaristaScope());
      return this;
    }

    /**
     * Finds the signature drinks by name and checks all objects. Each error has the line of code
     * that created the object.
     *
     * @throws IllegalStateException with the errors of all objects
     */
    public CoffeeShop build() {
      List<String> errors = new ArrayList<>();
      if (name == null) {
        errors.add("shop: name is not set");
      }

      Set<String> ids = new HashSet<>();
      Map<String, Drink> menu = new LinkedHashMap<>();
      for (Drink.Builder drink : drinks) {
        List<String> drinkErrors = drink.errors();
        errors.addAll(drinkErrors);
        if (drink.id() != null && !ids.add(drink.id())) {
          errors.add(drink.error("drink '" + drink.id() + "' already exists"));
        } else if (drinkErrors.isEmpty()) {
          menu.put(drink.id(), drink.build());
        }
      }

      for (Barista.Builder barista : baristas) {
        errors.addAll(barista.errors(menu));
      }

      Validation.throwIfAny("coffee shop", errors);
      return new CoffeeShop(name, List.copyOf(menu.values()),
          baristas.stream().map(barista -> barista.build(menu)).toList());
    }

    /**
     * Scope pattern: common settings for the next baristas. Each {@link #barista} applies the
     * defaults first, then its own configuration, so the configuration can change a default.
     */
    public final class BaristaScope {

      private Consumer<Barista.Builder> defaults = _ -> {
      };

      private BaristaScope() {
      }

      public BaristaScope defaults(Consumer<Barista.Builder> defaults) {
        this.defaults = defaults;
        return this;
      }

      public BaristaScope barista(Consumer<Barista.Builder> config) {
        Builder.this.barista(defaults.andThen(config));
        return this;
      }
    }
  }
}

package com.nbugaenco.softdesign.builder.model;

import java.util.ArrayList;
import java.util.List;

/**
 * An item of the menu. The name is the ID of the drink. Prices are in MDL.
 */
public record Drink(String name, int price, int volumeMl) {

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    return builder().name(name).price(price).volumeMl(volumeMl);
  }

  /**
   * The mutable model of a drink. The setters check each value at once, {@link #build()} checks
   * that the required fields are set.
   */
  public static final class Builder {

    private final String createdAt = Validation.callSite();
    private String name;
    private Integer price;
    private int volumeMl = 250;

    private Builder() {
    }

    public Builder name(String name) {
      this.name = Validation.text(name, "name");
      return this;
    }

    public Builder price(int price) {
      this.price = Validation.range(price, 1, 1000, "price");
      return this;
    }

    public Builder volumeMl(int volumeMl) {
      this.volumeMl = Validation.range(volumeMl, 30, 1000, "volumeMl");
      return this;
    }

    /**
     * @throws IllegalStateException if the name or the price is not set
     */
    public Drink build() {
      Validation.throwIfAny("drink", errors());
      return new Drink(name, price, volumeMl);
    }

    String id() {
      return name;
    }

    /**
     * Returns the message with the line of code that created this builder.
     */
    String error(String message) {
      return createdAt + ": " + message;
    }

    List<String> errors() {
      List<String> errors = new ArrayList<>();
      if (name == null) {
        errors.add(error("drink name is not set"));
      }
      if (price == null) {
        errors.add(error("drink price is not set"));
      }
      return errors;
    }
  }
}

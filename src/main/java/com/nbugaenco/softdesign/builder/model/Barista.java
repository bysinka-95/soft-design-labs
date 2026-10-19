package com.nbugaenco.softdesign.builder.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A barista of the shop. {@code signature} is a drink from the same shop menu, or {@code null}.
 */
public record Barista(String name, int experienceYears, Drink signature) {

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    Builder builder = builder().name(name).experienceYears(experienceYears);
    return signature == null ? builder : builder.signature(signature);
  }

  /**
   * The mutable model of a barista. The signature drink is set by its name (ID) or as an object.
   * {@link CoffeeShop.Builder} finds the drink by its name in the menu.
   */
  public static final class Builder {

    private final String createdAt = Validation.callSite();
    private String name;
    private int experienceYears;
    private String signatureName;
    private Drink signature;

    private Builder() {
    }

    public Builder name(String name) {
      this.name = Validation.text(name, "name");
      return this;
    }

    public Builder experienceYears(int years) {
      this.experienceYears = Validation.range(years, 0, 60, "experienceYears");
      return this;
    }

    /**
     * Sets the signature drink by its name. Only {@link CoffeeShop.Builder} can find a drink by
     * its name.
     */
    public Builder signature(String drinkName) {
      this.signatureName = Validation.text(drinkName, "signature");
      this.signature = null;
      return this;
    }

    public Builder signature(Drink drink) {
      this.signatureName = drink.name();
      this.signature = drink;
      return this;
    }

    /**
     * @throws IllegalStateException if the name is not set, or if the signature is only a name
     */
    public Barista build() {
      return build(null);
    }

    /**
     * Builds with the drinks of {@code menu}. A {@code null} menu uses the drink object.
     */
    Barista build(Map<String, Drink> menu) {
      Validation.throwIfAny("barista", errors(menu));
      return new Barista(name, experienceYears, find(menu));
    }

    List<String> errors(Map<String, Drink> menu) {
      List<String> errors = new ArrayList<>();
      if (name == null) {
        errors.add(createdAt + ": barista name is not set");
      }
      if (signatureName != null && find(menu) == null) {
        errors.add(createdAt + ": unknown drink '" + signatureName + "'");
      }
      return errors;
    }

    private Drink find(Map<String, Drink> menu) {
      return menu == null ? signature : menu.get(signatureName);
    }
  }
}

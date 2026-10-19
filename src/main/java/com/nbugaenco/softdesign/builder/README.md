# Lab 3: Builder

Theme: a coffee shop. The builder makes a `CoffeeShop` with its menu (`Drink`) and its `Barista`s.
A barista refers to a signature drink of the same menu.

The builders follow the Lombok `@Builder` style: `Drink.builder()...build()`, `drink.toBuilder()`,
and a nested `static final class Builder` in each record.

## Packages

| Package | Contents                                                                                |
|---------|-----------------------------------------------------------------------------------------|
| `model` | `CoffeeShop`, `Drink`, `Barista` (immutable records) with nested builders, `Validation` |

## Concepts

| Concept                            | Where                                                             |
|------------------------------------|-------------------------------------------------------------------|
| Initialization in one step         | `new Drink("latte", 45, 250)`                                     |
| Initialization step by step        | `Drink.Builder`: one call for each field                          |
| Immutable models                   | Records. `CoffeeShop` copies its lists with `List.copyOf`         |
| Mutable models of the builder      | The private fields of each `Builder`. `null` means "not set"      |
| Encapsulation of the setup         | Private builder fields, setters that check each value             |
| Object IDs                         | The drink name. `Barista.Builder.signature("latte")` refers to it |
| Validation                         | Setters (`Validation.text`, `range`) and `build()`                |
| Fluent builder                     | Each setter returns `this`                                        |
| Configuration delegate             | `drink(Consumer<Drink.Builder>)`, `barista(...)`, `scope(...)`    |
| Scope                              | `CoffeeShop.Builder.BaristaScope`                                 |
| Changed copy of an immutable model | `toBuilder()`                                                     |

## Task

| # | Requirement                                                     | Solution                                                 |
|---|-----------------------------------------------------------------|----------------------------------------------------------|
| 1 | A group of related classes: mutable models and immutable models | Records and their `Builder`s                             |
| 2 | Builders that set each property and make the immutable models   | `Drink.Builder`, `Barista.Builder`, `CoffeeShop.Builder` |
| 3 | Validation in the create function and in the setters            | `build()`, `Validation`                                  |
| 4 | Use in the main function                                        | `BuilderDemo.main`                                       |

Solved extra items:

| Extra item                                              | Solution                                                              |
|---------------------------------------------------------|-----------------------------------------------------------------------|
| Fluent builder, configuration delegates, scope          | `CoffeeShop.Builder`, `BaristaScope`                                  |
| Validation with the line of code, errors of all objects | `Validation.callSite()` (`StackWalker`), `CoffeeShop.Builder.build()` |
| Unit tests with a framework and snapshot tests          | `src/test/.../builder/ShopBuilderTest`, `Snapshot`                    |

## Run

```shell
java src/main/java/com/nbugaenco/softdesign/builder/BuilderDemo.java
./gradlew test --tests '*builder*'
```

The snapshot files are in `src/test/resources/snapshots`. To accept a new result, delete the file and
run the tests again.

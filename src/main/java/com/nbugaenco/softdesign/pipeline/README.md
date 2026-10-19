# Lab 2: Pipeline

Theme: a coffee shop. One pipeline makes and sells an order. Another pipeline changes the stock.

## Packages

| Package     | Contents                                              |
|-------------|-------------------------------------------------------|
| `core`      | `Context`, `Step`, `Pipeline`                         |
| `decorator` | `Logged`, `Repeat`                                    |
| `order`     | `Order` context, `Brew`, `AddExtra`, `Pay`, `Receipt` |
| `stock`     | `Stock` context, `Restock`, `Take`                    |

## Concepts

| Concept                          | Where                                                       |
|----------------------------------|-------------------------------------------------------------|
| Abstraction, polymorphism        | `Step<C>` interface                                         |
| Context                          | `Context`, `Order`, `Stock`                                 |
| Strategy (action as a parameter) | `Pipeline.wrapAll(type, wrap)`                              |
| Introspection                    | `Step.describe(StringBuilder)`, `Pipeline.print()`          |
| Data-oriented (steps are data)   | Steps are records, so `equals` and `toString` come for free |
| Decorator                        | `Logged`, `Repeat`, `Pipeline` inside a `Pipeline`          |

## Task

| # | Requirement                          | Solution                     |
|---|--------------------------------------|------------------------------|
| 1 | A pipeline on your own theme         | `Pipeline`, `Order`, `Stock` |
| 2 | A demo pipeline in the main function | `PipelineDemo.main`          |

Solved extra items:

| Extra item                                   | Solution                                                         |
|----------------------------------------------|------------------------------------------------------------------|
| Generic context, 2 context types             | `Pipeline<C extends Context>`, `Step<C>`, `Order`, `Stock`       |
| Introspection with `StringBuilder` + helpers | `Step.describe(StringBuilder)`, `describe()`, `Pipeline.print()` |
| 2 decorators + a nested pipeline             | `Logged`, `Repeat`, `Pipeline implements Step`                   |
| 3+ methods that change the steps             | `replaceFirst`, `replaceAll`, `wrapAll`, `moveTo`                |
| Singleton for a step with no state           | `Receipt.INSTANCE` (enum singleton)                              |
| Unit tests                                   | `src/test/.../pipeline/core/PipelineTest`                        |
| Responsibility chain                         | `Context.stop()`, `isDone()`. `Pay` and `Take` call `stop()`.    |

## Run

```shell
java src/main/java/com/nbugaenco/softdesign/pipeline/PipelineDemo.java
./gradlew test --tests '*pipeline*'
```

# Лаба 3: Builder — полный гид

Этот файл объясняет лабу целиком: идею, каждый файл, каждый паттерн и ключевые строки кода.
Краткая таблица «требование → решение» лежит в [README.md](README.md).

## Содержание

1. [Что такое builder](#1-что-такое-builder)
2. [Как это выглядит у нас](#2-как-это-выглядит-у-нас)
3. [Файлы](#3-файлы)
4. [Паттерны и механизмы](#4-паттерны-и-механизмы)
5. [Разбор кода по файлам](#5-разбор-кода-по-файлам)
6. [Трассировка демо](#6-трассировка-демо)
7. [Тесты](#7-тесты)
8. [C# из видео → Java у нас](#8-c-из-видео--java-у-нас)
9. [Вопросы на защите](#9-вопросы-на-защите)

---

## 1. Что такое builder

**Builder (строитель)** — объект, который собирает другой объект (или группу объектов) по шагам,
а в конце одним вызовом `build()` выдаёт готовый результат.

Объект можно создать двумя способами.

**В один этап** — все данные передаются сразу:

```java
Drink latte = new Drink("latte", 45, 250);
```

Это просто, но есть проблемы:

- Аргументы читаются плохо: что такое `45` и `250`? Легко перепутать порядок.
- Необязательные поля приводят к куче конструкторов (`Drink(name, price)`, `Drink(name, price, volume)`, …).
  Это называют «телескопическими конструкторами».
- Группа связанных объектов собирается неудобно. Бариста ссылается на напиток, значит напиток
  надо создать раньше, хранить в переменной и передавать вручную.
- Проверка «объект правильный» размазана по коду, который его создаёт.

**Поэтапно (builder)** — данные задаются по одному, в любом порядке:

```java
CoffeeShop.builder()
    .name("Bean There")
    .drink(d -> d.name("latte").price(45))
    .barista(b -> b.name("Ana").signature("latte"))
    .build();
```

Что это даёт:

- Каждое значение подписано именем метода (`price(45)`).
- Необязательные поля можно не задавать, сработает значение по умолчанию.
- Ссылки между объектами задаются по ID (`signature("latte")`), порядок объявления не важен.
- Вся проверка собрана в одном месте: в сеттерах и в `build()`.
- На выходе — неизменяемый (immutable) объект, который гарантированно прошёл проверку.

### Классический Java-builder (Effective Java, Lombok)

В Java сложился стандартный вид builder-а. Его описывает Джошуа Блох в «Effective Java»
(Item 2), а библиотека Lombok генерирует его аннотацией `@Builder`. Мы пишем его вручную в том же виде:

| Элемент                                 | Зачем                                                        |
|-----------------------------------------|--------------------------------------------------------------|
| `public static Builder builder()`       | Точка входа. Конструктор билдера закрыт (`private`).         |
| `public static final class Builder`     | Вложен в класс, который строит. Видно, что чей.              |
| `private`-поля в `Builder`              | Это и есть mutable модель (черновик).                        |
| сеттеры возвращают `this`               | Fluent-цепочка.                                              |
| `public Drink build()`                  | Проверка + создание неизменяемого объекта.                   |
| `public Builder toBuilder()`            | Билдер, заполненный данными готового объекта: для изменённой копии. |
| `drink(Drink)` в билдере коллекции      | Как `@Singular` в Lombok: добавить один элемент списка.      |

Сверх Lombok у нас есть делегаты конфигурации, scope, ID-ссылки, сбор всех ошибок и строка кода
в ошибке.

## 2. Как это выглядит у нас

Тема — кофейня. Группа связанных объектов:

| Объект       | Поля                                    | Связи                                       |
|--------------|-----------------------------------------|---------------------------------------------|
| `CoffeeShop` | `name`, `menu`, `baristas`              | содержит напитки и бариста                  |
| `Drink`      | `name` (это ID), `price`, `volumeMl`    |                                             |
| `Barista`    | `name`, `experienceYears`, `signature`  | `signature` — напиток из меню этого же кафе |

У каждого record-а есть вложенный `Builder`:

```
CoffeeShop ──────────── CoffeeShop.Builder
                          ├── List<Drink.Builder>     (черновики напитков)
                          ├── List<Barista.Builder>   (черновики бариста)
                          └── BaristaScope            (общие настройки бариста)
Drink ───────────────── Drink.Builder
Barista ─────────────── Barista.Builder
```

Поток данных:

```
 ваш код                         черновики (mutable)              результат (immutable)
──────────                     ────────────────────             ──────────────────────
.drink(d -> d.price(45)) ──►  Drink.Builder.price = 45
                              (сеттер проверяет значение)
.barista(b -> ...)       ──►  Barista.Builder.signatureName = "latte"
.build()                 ──►  CoffeeShop.Builder.build():
                              1. ошибки всех черновиков
                              2. имена → ссылки на Drink
                              3. Drink.Builder.build() ...   ──►  Drink, Barista, CoffeeShop
```

Ключевая идея: `CoffeeShop.Builder` хранит **черновики** дочерних объектов, а не готовые объекты.
Поэтому он может проверить их все вместе и найти связи между ними.

## 3. Файлы

```
builder/
├── model/
│   ├── CoffeeShop.java     record + Builder (+ BaristaScope внутри Builder)
│   ├── Drink.java          record + Builder
│   ├── Barista.java        record + Builder
│   └── Validation.java     проверки значений, сбор ошибок, строка кода
└── BuilderDemo.java        демо

test/.../builder/
├── ShopBuilderTest.java    unit-тесты
└── Snapshot.java           мини-библиотека snapshot-тестов
test/resources/snapshots/   сохранённые снимки: shop.txt, errors.txt
```

Правило «один класс — один файл» здесь нарушено специально: builder вложен в класс, который строит.
Так принято в Java (`HttpRequest.Builder`, `Locale.Builder` в JDK, Lombok `@Builder`).
Билдер — часть API своего класса, отдельно он не имеет смысла.

Все классы в одном пакете. Это нужно: `CoffeeShop.Builder` вызывает package-private методы
`Drink.Builder` (`errors()`, `id()`), которые не видны снаружи пакета.

## 4. Паттерны и механизмы

### 4.1 Инициализация в один этап

**Что это.** Объект получает все данные в конструкторе и после этого готов.

**У нас.** Так создаются все record-ы — внутри `build()`:

```java
return new Drink(name, price, volumeMl);
```

В этот момент данные уже проверены. Конструктор record-а публичный (Java требует, чтобы канонический
конструктор record-а был не менее доступен, чем сам record), поэтому `new Drink(...)` без билдера
тоже возможен. Проверку он обходит — это осознанная цена простых record-ов.

### 4.2 Инициализация поэтапно

**Что это.** Объект заполняется по частям, несколькими вызовами. Пока он не закончен, им
нельзя пользоваться как результатом.

**У нас.** Каждый вызов `name(...)`, `price(...)`, `drink(...)` — один шаг. Промежуточное
состояние живёт в полях `Builder`-а. Готовый результат появляется только после `build()`.

### 4.3 Immutable модели: плюсы и минусы

**Что это.** Immutable (неизменяемый) объект после создания нельзя изменить. «Изменение» = создание
нового объекта.

**У нас.** Все модели — record-ы: их поля `final`. Списки защищены отдельно:

```java
public record CoffeeShop(String name, List<Drink> menu, List<Barista> baristas) {

  public CoffeeShop {
    menu = List.copyOf(menu);
    baristas = List.copyOf(baristas);
  }
  ...
}
```

`final`-поле со списком не мешает менять сам список (`menu().add(...)`). Поэтому компактный
конструктор record-а заменяет переданные списки неизменяемыми копиями. Теперь `shop.menu().add(...)`
бросает `UnsupportedOperationException` (тест `builtModelCannotChange`).

Плюсы (из задания):

- **Гарантия неизменяемости.** Объект прошёл проверку и больше не может стать неправильным.
  Его можно отдавать куда угодно, в том числе в другие потоки, без блокировок.
- **Избежание копий.** Объект не меняется, значит его не надо копировать «на всякий случай».
  Один и тот же `Drink` лежит и в `menu`, и в `Barista.signature`. `List.copyOf` от уже
  неизменяемого списка не копирует, а возвращает тот же список.

Минусы:

- **Изменение требует пересоздания.** Чтобы поменять цену латте, нужен новый `Drink`, новый
  `Barista` (если латте его фирменный) и новый `CoffeeShop`.
- **Лишняя память.** Каждое такое «изменение» выделяет новые объекты.

Минус «изменение требует пересоздания» у нас закрывает `toBuilder()` (4.12).

### 4.4 Mutable модели в контексте билдера

**Что это.** Изменяемая форма данных, которую удобно заполнять по шагам.

**У нас.** Mutable модель — это поля самого билдера (так устроен Lombok `@Builder`):

```java
public static final class Builder {

  private final String createdAt = Validation.callSite();
  private String name;
  private Integer price;
  private int volumeMl = 250;
  ...
}
```

Отличия от immutable модели:

- Поля не `final`, их меняют сеттеры.
- `Integer price`, а не `int`: `null` значит «цену не задали». У `int` нет такого значения,
  а `0` можно спутать с настоящей ошибкой.
- `volumeMl = 250` — значение по умолчанию для необязательного поля.
- `Barista.Builder` хранит `signatureName` (ID напитка), а не только `Drink`. См. 4.6.
- Служебное поле `createdAt`, которого нет в результате. См. 4.8.

Почему не отдельный класс `MutableDrink` с публичными полями (как в видео): в Java принято, что
состояние билдера и есть черновик. Отдельный класс добавил бы ещё один слой, который всё равно
виден только билдеру.

### 4.5 Инкапсуляция настройки объекта

**Что это.** Данные черновика закрыты. Изменить их можно только методами, которые знают правила.

**У нас.**

```java
private Integer price;                         // снаружи не видно

private Builder() {                            // создать можно только через Drink.builder()
}

public Builder price(int price) {
  this.price = Validation.range(price, 1, 1000, "price");   // правило живёт здесь
  return this;
}
```

- Пользователь видит только методы. Записать цену `-5` невозможно.
- Черновик нельзя получить «наполовину» как готовый объект: результат даёт только `build()`.
- `CoffeeShop.Builder` получает данные дочерних билдеров через package-private методы (`errors()`,
  `id()`), а не через поля. Снаружи пакета этих методов не видно.

В видео инкапсуляция вынесена в другой класс: модель `MutableCitizen` открыта, а правила живут в
`CitizenBuilder`. У нас тот же принцип «модель защищает билдер», только модель и билдер — один класс.

### 4.6 Идентификаторы объектов

**Что это.** Вместо прямой ссылки на объект хранится его ключ (ID). Объект находят по ключу позже.

**У нас.** ID напитка — его имя. Бариста может сослаться на напиток двумя способами:

```java
.barista(b -> b.name("Ana").signature("flat white"))   // по ID
.barista(b -> b.name("Maria").signature(espresso))     // объектом
```

Внутри `Barista.Builder` в обоих случаях запоминается имя:

```java
public Builder signature(Drink drink) {
  this.signatureName = drink.name();
  this.signature = drink;
  return this;
}
```

`CoffeeShop.Builder` всегда ищет напиток по имени в своём меню. Поэтому у бариста в результате
гарантированно напиток **из этого же кафе**.

Зачем ID:

- **Порядок не важен.** Бариста можно объявить раньше напитка: имена превращаются в ссылки только
  в `build()`, когда все напитки уже известны.
- **Не нужны переменные.** Не надо хранить билдер напитка в переменной, чтобы передать его
  бариста. Fluent-цепочка не рвётся (в видео приходится писать `var home = city.Home(); ... s.Home(home);`).
- **Проверяемость.** `build()` находит ссылку на несуществующий напиток (`unknown drink 'mocha'`)
  и повтор ID (`drink 'latte' already exists`).

Отдельный `Barista.builder().signature("latte").build()` (без кафе) упадёт с `unknown drink 'latte'`:
искать по имени негде. Отдельно от кафе надо передавать сам `Drink`.

### 4.7 Валидация: два уровня

Задание требует проверку и в функции создания, и на уровне методов. Это разные проверки.

**Уровень 1 — сеттер (контракт одного значения).** Проверяет одно значение сразу:

```java
static int range(int value, int min, int max, String field) {
  if (value < min || value > max) {
    throw new IllegalArgumentException(
        field + " must be in [" + min + ", " + max + "], but was " + value);
  }
  return value;
}
```

- Ошибка видна сразу на нужной строке: stack trace указывает на `price(-5)`.
- `IllegalArgumentException` — стандартное исключение для «плохой аргумент».

**Уровень 2 — `build()` (проверка объекта целиком и связей).** Сеттер не может знать:

- задали ли обязательное поле вообще (сеттер просто не вызвали);
- нет ли второго напитка с тем же именем;
- есть ли в меню напиток, на который ссылается бариста.

Каждый дочерний билдер умеет вернуть свои ошибки списком:

```java
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
```

`Drink.Builder.build()` бросает их сразу. `CoffeeShop.Builder.build()` сначала **собирает ошибки
всех** черновиков, добавляет свои (повтор ID, неизвестный напиток) и бросает одно исключение:

```java
static void throwIfAny(String object, List<String> errors) {
  if (!errors.isEmpty()) {
    throw new IllegalStateException("Invalid " + object + ":\n" + String.join("\n", errors));
  }
}
```

- `IllegalStateException` — «объект не в том состоянии, чтобы выполнить операцию».
- Все ошибки сразу, а не только первая: исправить можно всё за один заход.

### 4.8 Строка кода, где создан объект

**Что это.** В каждой ошибке указано, где в коде создан проблемный объект:

```
BuilderDemo.java:47: drink name is not set
```

Без этого ошибка «drink name is not set» при 20 напитках бесполезна.

**Как.** Поле `createdAt` каждого билдера задаётся при создании билдера:

```java
private final String createdAt = Validation.callSite();
```

```java
private static final StackWalker WALKER =
    StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

static String callSite() {
  return WALKER.walk(frames -> frames
      .filter(f -> f.getDeclaringClass().getPackage() != Validation.class.getPackage())
      .findFirst()
      .map(f -> f.getFileName() + ":" + f.getLineNumber())
      .orElse("unknown"));
}
```

- `StackWalker` (Java 9+) — ленивый обход стека. Дешевле, чем `new Throwable().getStackTrace()`,
  который собирает весь стек сразу.
- `RETAIN_CLASS_REFERENCE` нужен, чтобы вызвать `getDeclaringClass()`.
- Фильтр пропускает кадры пакета `model`. Стек в момент создания: `Validation.callSite` ←
  `Drink.Builder.<init>` ← `Drink.builder` ← `CoffeeShop.Builder.drink` ← **ваш код**. Первый
  кадр вне пакета — нужная строка.
- Внутри `scope` вызов `.barista(...)` стоит в лямбде. Лямбда компилируется в метод того класса,
  где она написана (`BuilderDemo`), поэтому в ошибке будет строка этой лямбды.

Из-за этого фильтра тест лежит в пакете `builder`, а не `model`. Иначе тестовый класс тоже
считался бы «внутренним», и в ошибке была бы строка из JUnit.

### 4.9 Fluent builder

**Что это.** Каждый метод билдера возвращает `this`, поэтому вызовы пишутся цепочкой.

```java
Drink.builder().name("espresso").price(30).volumeMl(40).build();
```

Без fluent пришлось бы писать каждый вызов отдельной строкой с повтором переменной.

### 4.10 Делегат конфигурации

**Что это.** Метод принимает функцию, которая настраивает новый объект. Метод сам создаёт билдер,
отдаёт его функции, потом продолжает работу.

```java
public Builder drink(Consumer<Drink.Builder> config) {
  Drink.Builder drink = Drink.builder();
  config.accept(drink);
  drinks.add(drink);
  return this;
}
```

`Consumer<T>` — стандартный функциональный интерфейс «принять `T`, ничего не вернуть».

Что это даёт:

- **Цепочка не рвётся.** `drink(...)` возвращает `CoffeeShop.Builder`, поэтому можно сразу
  писать `.drink(...).barista(...)`.
- **Видна структура.** Настройка напитка визуально вложена в вызов `drink`.
- **Билдер управляет жизненным циклом.** Он создаёт дочерний билдер и сам добавляет его в список.
  Забыть добавить невозможно. Если делегат упал (сеттер бросил исключение), черновик в список
  не попадает.

Есть и перегрузка без делегата — `drink(Drink)`, как `@Singular` в Lombok. Она добавляет готовый
напиток через `drink.toBuilder()`, то есть тоже как черновик.

### 4.11 Паттерн Scope

**Что это.** Группа настроек, общая для нескольких следующих объектов. Аналог «по умолчанию для
этого блока».

```java
.scope(s -> s
    .defaults(b -> b.experienceYears(1).signature("latte"))
    .barista(b -> b.name("Ion"))
    .barista(b -> b.name("Maria").signature(espresso)))
```

Ion и Maria получают `experienceYears = 1`. Ion получает `signature = latte`, а Maria заменяет её
на `espresso`.

**Как.** Настройки по умолчанию — это тоже делегат. Scope склеивает два делегата:

```java
public final class BaristaScope {

  private Consumer<Barista.Builder> defaults = _ -> {
  };

  public BaristaScope barista(Consumer<Barista.Builder> config) {
    Builder.this.barista(defaults.andThen(config));
    return this;
  }
}
```

- `a.andThen(b)` — новый `Consumer`, который сначала вызывает `a`, потом `b`. Собственная настройка
  бариста идёт **после** значений по умолчанию и может их перезаписать.
- `BaristaScope` — **внутренний** (не `static`) класс `CoffeeShop.Builder`. Каждый scope привязан
  к своему билдеру кафе: `Builder.this` — ссылка на этот внешний билдер.
- `_ -> {}` — «ничего не делать», чтобы `barista(...)` работал и без `defaults(...)`. `_` —
  безымянный параметр (Java 22+).

Сравнение с видео. Там `CityScopeBuilder` хранит шаблон `MutableCitizen` и:

- повторяет все сеттеры (`Age`, `Name`, `Home`) — для этого нужен общий интерфейс `ICitizenBuilder`;
- вручную копирует каждое поле шаблона в новый объект (`if (... != InvalidAge) citizen.Model.Age = ...`).

У нас scope хранит не данные, а **действие**. Поэтому:

- сеттеры не повторяются: в `defaults` приходит обычный `Barista.Builder`;
- копировать поля не надо: действие просто выполняется ещё раз для каждого бариста;
- значения по умолчанию проходят те же проверки сеттеров.

### 4.12 `toBuilder()`: изменённая копия immutable объекта

**Что это.** Метод готового объекта, который возвращает билдер, заполненный его данными.
Так же работает `@Builder(toBuilder = true)` в Lombok.

```java
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
```

```java
CoffeeShop bigger = shop.toBuilder()
    .drink(d -> d.name("mocha").price(55))
    .build();      // shop не изменился
```

Это ответ на минус immutable моделей «изменение требует пересоздания»: пересоздание остаётся,
но писать его вручную не надо. Новый объект снова проходит всю проверку.

### 4.13 Интерфейсы — почему их нет

Среди концептов есть «создание имплементации согласно интерфейсу» и «использование интерфейсов
для удаления повторяющегося кода». В видео интерфейсы нужны для двух вещей:

1. **Общие сеттеры у `CitizenBuilder` и `CityScopeBuilder`.** У нас эту задачу решает композиция
   делегатов (4.11): повторяющегося кода нет, интерфейс не нужен.
2. **Несколько реализаций одного билдера** (`CityBuilder`, `CityXBuilder`, абстрактный билдер).
   У нас одна реализация. Интерфейс с одной реализацией — лишний слой.

Интерфейс стоит добавить вместе с абстрактным билдером (это отдельное дополнительное задание,
мы его не выбирали).

### 4.14 Snapshot-тесты

**Что это.** Тест сохраняет результат в файл при первом запуске. При следующих запусках результат
сравнивается с файлом. Удобно для больших результатов, которые неудобно описывать в `assertThat`
по одному полю: многострочный отчёт об ошибках, вывод всей структуры.

```java
static void assertMatches(String name, String actual) throws IOException {
  Path file = DIR.resolve(name + ".txt");
  if (Files.notExists(file)) {
    Files.createDirectories(DIR);
    Files.writeString(file, actual);
  }
  assertThat(actual).isEqualTo(Files.readString(file));
}
```

Как пользоваться:

- Первый запуск создаёт `src/test/resources/snapshots/<name>.txt`. Файл надо **просмотреть
  глазами** и закоммитить: тест в этот момент проходит всегда.
- Если код изменился и новый результат правильный — удалить файл и запустить тесты снова.
- Если результат изменился случайно — тест упадёт и покажет разницу.

Готовые библиотеки есть (например, `java-snapshot-testing`), но для лабы хватает 10 строк без
новой зависимости.

## 5. Разбор кода по файлам

### `model/Drink.java`

| Элемент              | Что делает                                                   |
|----------------------|--------------------------------------------------------------|
| `record Drink`       | `name`, `price`, `volumeMl`. Java генерирует `equals`/`toString`. |
| `builder()`          | Новый пустой билдер.                                         |
| `toBuilder()`        | Билдер с данными этого напитка.                              |
| `Builder.name`       | Не пустое.                                                   |
| `Builder.price`      | От 1 до 1000.                                                |
| `Builder.volumeMl`   | От 30 до 1000, по умолчанию 250.                             |
| `Builder.build()`    | Бросает все ошибки напитка или возвращает `Drink`.           |
| `Builder.id()`       | package-private: имя напитка для `CoffeeShop.Builder`.       |
| `Builder.error(msg)` | package-private: `msg` со строкой кода, где создан билдер.   |
| `Builder.errors()`   | package-private: список ошибок без исключения.               |

### `model/Barista.java`

| Элемент                    | Что делает                                                   |
|----------------------------|--------------------------------------------------------------|
| `record Barista`           | `name`, `experienceYears`, `signature` (`Drink` или `null`). |
| `toBuilder()`              | Копирует поля, `signature` передаёт объектом.                |
| `Builder.experienceYears`  | От 0 до 60, по умолчанию 0.                                  |
| `Builder.signature(String)`| Ссылка по ID. Найдёт только `CoffeeShop.Builder`.            |
| `Builder.signature(Drink)` | Ссылка объектом. Имя тоже запоминается.                     |
| `Builder.build()`          | Отдельно от кафе: использует сам объект `Drink`.             |
| `Builder.build(menu)`      | package-private: ищет напиток по имени в `menu`.             |
| `Builder.errors(menu)`     | package-private: нет имени, неизвестный напиток.             |

`find(menu)` — одно место, где решается, откуда брать напиток: `menu == null` → объект,
иначе → поиск по имени в меню.

### `model/CoffeeShop.java`

| Элемент                   | Что делает                                                       |
|---------------------------|------------------------------------------------------------------|
| компактный конструктор    | `List.copyOf` для обоих списков.                                 |
| `toBuilder()`             | Билдер со всеми напитками и бариста (как черновиками).           |
| `Builder.name`            | Имя кафе.                                                        |
| `Builder.drink(Drink)`    | Добавить готовый напиток (`@Singular`-стиль).                    |
| `Builder.drink(Consumer)` | Добавить напиток через делегат.                                  |
| `Builder.barista(...)`    | То же для бариста.                                               |
| `Builder.scope(Consumer)` | Создать `BaristaScope` и отдать его делегату.                   |
| `Builder.build()`         | Проверить всё, найти ссылки, создать `CoffeeShop`.              |

`build()` по шагам:

1. Нет имени кафе — ошибка.
2. Проход по черновикам напитков:
   - ошибки черновика (нет имени, нет цены) — в общий список;
   - имя уже встречалось (`Set<String> ids`) — ошибка «already exists»;
   - иначе, если ошибок нет, — `drink.build()` в `LinkedHashMap<String, Drink> menu`. Это
     одновременно индекс по ID и меню в порядке объявления.
3. Проход по бариста: `errors(menu)` — нет имени, неизвестный напиток.
4. `throwIfAny` — одно исключение со всеми ошибками.
5. Нет ошибок — `new CoffeeShop(...)`, каждый бариста строится с `build(menu)`.

Напитки обрабатываются **до** бариста, поэтому к шагу 3 меню уже полное — отсюда независимость
от порядка объявления.

### `model/Validation.java`

package-private утилита (`final`, `private`-конструктор):

| Метод        | Что делает                                                       |
|--------------|------------------------------------------------------------------|
| `text`       | Не `null` и не пустая строка, иначе `IllegalArgumentException`.  |
| `range`      | Число в диапазоне, иначе `IllegalArgumentException`.             |
| `throwIfAny` | Если список не пуст — `IllegalStateException` со всеми ошибками. |
| `callSite`   | `Файл.java:строка` первого вызова вне пакета `model`.            |

`text` и `range` возвращают проверенное значение, поэтому сеттер — одна строка:
`this.price = Validation.range(price, 1, 1000, "price");`.

### `BuilderDemo.java`

`static void main()` без `public` и аргументов (Java 25). Запуск без Gradle:

```shell
java src/main/java/com/nbugaenco/softdesign/builder/BuilderDemo.java
```

## 6. Трассировка демо

**Часть 1 — правильное кафе.**

| Вызов                                                 | Результат                                        |
|-------------------------------------------------------|--------------------------------------------------|
| `Drink.builder()...build()` → `drink(espresso)`       | `Drink[espresso, 30, 40]`, создан отдельно       |
| `drink(latte, 45)`                                    | `Drink[latte, 45, 250]` — объём по умолчанию     |
| `drink(flat white, 50, 180)`                          | `Drink[flat white, 50, 180]`                     |
| `barista(Ana, 5, "flat white")`                       | `Barista[Ana, 5, flat white]` — ссылка по ID     |
| scope `defaults(1, "latte")` + `barista(Ion)`         | `Barista[Ion, 1, latte]`                         |
| scope + `barista(Maria, espresso)`                    | `Barista[Maria, 1, espresso]` — default заменён, ссылка объектом |

**Часть 2 — `toBuilder`.** К копии добавлены `mocha` и бариста `Dan`. В новом кафе 4 напитка,
в исходном по-прежнему 3.

**Часть 3 — проверка в сеттере.** `Drink.builder().price(-5)` сразу бросает
`price must be in [1, 1000], but was -5`. До `build()` дело не доходит.

**Часть 4 — проверка в `build()`.** Одно исключение, все ошибки:

```
Invalid coffee shop:
shop: name is not set                          ← не вызвали name(...)
BuilderDemo.java:46: drink price is not set    ← второй latte без цены
BuilderDemo.java:46: drink 'latte' already exists
BuilderDemo.java:47: drink name is not set     ← напиток без имени
BuilderDemo.java:48: barista name is not set
BuilderDemo.java:48: unknown drink 'mocha'     ← mocha нет в меню
```

У одного объекта может быть несколько ошибок, и все они выводятся.

## 7. Тесты

`src/test/java/com/nbugaenco/softdesign/builder/ShopBuilderTest.java` (JUnit 5 + AssertJ):

| Тест                                       | Что проверяет                                                          |
|--------------------------------------------|------------------------------------------------------------------------|
| `buildsShop`                               | Вся структура кафе, snapshot `shop.txt`.                               |
| `scopeAppliesDefaultsAndConfigChangesThem` | Ion получил значения scope, Maria заменила `signature`.                |
| `toBuilderMakesEqualCopyThatCanChange`     | `toBuilder().build()` равен оригиналу; изменённая копия не трогает оригинал. |
| `setterRejectsInvalidValue`                | `volumeMl(5)` бросает `IllegalArgumentException` с точным текстом.     |
| `childBuilderChecksRequiredFields`         | `Barista.builder()` отдельно от кафе: нет имени, ссылка по имени не найдена. |
| `buildReportsAllErrorsWithLines`           | Все ошибки со строками теста, snapshot `errors.txt`.                   |
| `builtModelCannotChange`                   | `menu().add(...)` бросает `UnsupportedOperationException`.             |

`toBuilderMakesEqualCopyThatCanChange` работает благодаря record-ам: `equals` сравнивает поля,
списки сравниваются поэлементно.

В `errors.txt` записаны номера строк `ShopBuilderTest.java`. Если вставить строки выше
`buildReportsAllErrorsWithLines`, snapshot устареет: удалите файл и запустите тесты снова.

Запуск:

```shell
./gradlew test --tests '*builder*'
```

## 8. C# из видео → Java у нас

| C# (видео)                                     | Java (у нас)                                  | Почему так                                        |
|------------------------------------------------|-----------------------------------------------|---------------------------------------------------|
| `City`, `Citizen`, `Home`                      | `CoffeeShop`, `Barista`, `Drink`              | Своя тема.                                        |
| `sealed class` + `required ... { get; init; }` | `record`                                      | Record = неизменяемые поля + `equals`/`toString`. |
| `Citizen[] Citizens`                           | `List<Barista>` + `List.copyOf`               | Массив в Java всегда можно изменить.              |
| `MutableCitizen` (отдельный класс)             | `private`-поля в `Builder`                    | Стандарт Java / Lombok: состояние билдера = черновик. |
| `MutableCitizen.InvalidAge = -1`               | `Integer price = null`                        | `null` честнее «магического» числа.               |
| `new CityBuilder()`                            | `CoffeeShop.builder()`                        | Статическая фабрика, конструктор закрыт.          |
| `CitizenBuilder` возвращает `void`             | сеттеры возвращают `this`                     | Fluent builder.                                   |
| `var home = city.Home(); john.Home(home);`     | `signature("latte")` по имени                 | ID: цепочка не рвётся, порядок не важен.          |
| `Dictionary<MutableHome, Home>` в `Build()`    | `Map<String, Drink> menu`                     | Ссылки по ID, а не по объекту черновика.          |
| `throw` на первой ошибке                       | список ошибок + одно исключение               | Дополнительное задание.                           |
| —                                              | `createdAt` + `StackWalker`                   | Дополнительное задание: строка кода.              |
| `CityScopeBuilder` + `ICitizenBuilder`         | `BaristaScope` + `defaults.andThen(config)`   | Без дублирования сеттеров и интерфейса.           |
| —                                              | `toBuilder()`                                 | Изменённая копия immutable объекта.               |
| `ICityBuilder`, `CityXBuilder`, `DynamicCity`  | нет                                           | Абстрактный билдер не выбран (4.13).              |

## 9. Вопросы на защите

**Зачем builder, если есть конструктор?**
Поэтапная сборка, подписанные значения, необязательные поля, ссылки между объектами по ID, проверка
в одном месте, на выходе неизменяемый объект.

**Где здесь mutable модель?**
Это `private`-поля `Builder`-а. Так устроен стандартный Java-builder (Effective Java, Lombok).
Отдельный класс с публичными полями был бы ещё одним слоем, который видит только билдер.

**Почему `Builder` вложен в record, хотя у нас правило «один класс — один файл»?**
Билдер — часть API своего класса. Так сделано в JDK (`HttpRequest.Builder`) и в Lombok.

**Почему конструктор `Builder` — `private`?**
Единственная точка входа — `Drink.builder()`. Её можно поменять (например, вернуть заранее
настроенный билдер), не ломая пользователей.

**Почему `Integer`, а не `int`?**
`null` = «не задано». Иначе нельзя отличить «цену забыли» от «цена 0».

**Зачем две проверки — в сеттере и в `build()`?**
Сеттер проверяет одно значение и падает сразу на нужной строке. `build()` проверяет то, что видно
только целиком: обязательные поля, повторы ID, ссылки на несуществующие объекты.

**Почему `CoffeeShop.Builder` хранит черновики, а не готовые `Drink`?**
Чтобы проверить все объекты вместе, собрать все ошибки и найти ссылки по ID в одном `build()`.

**Почему `build()` не падает на первой ошибке?**
Чтобы показать все проблемы сразу и исправить их за один заход.

**Как билдер узнаёт строку кода?**
При создании билдер вызывает `StackWalker` и берёт первый кадр стека вне пакета `model`.

**Как работает scope?**
Хранит делегат по умолчанию и для каждого бариста вызывает `defaults.andThen(config)`. Собственная
настройка идёт второй и может перезаписать default.

**Зачем `toBuilder()`?**
Чтобы получить изменённую копию immutable объекта, не перечисляя все поля вручную.

**Можно ли сослаться на напиток, который объявлен позже?**
Да. Ссылка — это имя, а имена превращаются в объекты в `build()`, после обработки всех напитков.

**Почему `shop.menu().add(...)` падает?**
`CoffeeShop` в компактном конструкторе заменяет списки на `List.copyOf` — неизменяемые копии.

**Что такое snapshot-тест и когда он опасен?**
Сравнение с сохранённым файлом. Первый запуск проходит всегда, поэтому новый snapshot надо
проверить глазами.

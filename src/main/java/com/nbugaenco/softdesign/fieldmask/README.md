# Lab 1: Field Mask

## Concepts

- Encapsulation
- Domain model
- Masks as a set of boolean values
- Stronger abstractions that use simpler abstractions

## Task

| # | Requirement                                                                                | Solution                                          |
|---|--------------------------------------------------------------------------------------------|---------------------------------------------------|
| 1 | A domain model class with at least 5 fields, including `int`, `string`, `float` and `enum` | `entity/Book`, `entity/Genre`                     |
| 2 | An abstraction for a database that stores these objects                                    | `repository/BookRepository` (Spring Data JPA, H2) |
| 3 | A field mask class, first with `bool` values                                               | `mask/BoolFieldMask`                              |
| 4 | A database method that finds all objects by one field                                      | `BookRepository.findByTitle(String)`              |
| 5 | A static function that writes the masked fields to the console                             | `FieldMask.print`, `BoolFieldMask.print`          |
| 6 | Tests for the program                                                                      | `src/test/.../fieldmask`                          |

Solved extra items:

| Extra item                                                                      | Solution                                |
|---------------------------------------------------------------------------------|-----------------------------------------|
| A JSON REST API that returns objects with only some fields                      | `controller/BookController`             |
| A method that merges the objects with equal masked fields                       | `BookService.merge`                     |
| A method that copies masked fields from a source object to all matching objects | `BookService.copy`                      |
| Unit tests with a test framework                                                | JUnit 5 and AssertJ                     |
| A field mask that uses bits                                                     | `mask/FieldMask`                        |
| 3 methods that combine masks                                                    | `FieldMask.union`, `intersect`, `minus` |

## Domain model

`Book` has these fields:

| Field    | Type           |
|----------|----------------|
| `id`     | `Long`         |
| `title`  | `String`       |
| `author` | `String`       |
| `pages`  | `int`          |
| `rating` | `float`        |
| `genre`  | `Genre` (enum) |

The Flyway migration `db/migration/V1__create_book.sql` creates the `book` table and adds 5 books.

## Field masks

`BookField` is an enum with one constant for each field of `Book`, except `id`.
Each constant holds a getter and a setter.
Thus, the mask code can read and copy any field without a `switch` for each field.

### Boolean mask

`BoolFieldMask` is the first version. It is a record with one `boolean` for each field.
The method `has(BookField)` uses a `switch` to find the correct `boolean`.
The method `toFieldMask()` converts the boolean mask to the bit mask.

### Bit mask

`FieldMask` is the second version. It keeps all fields in one `int`.
Each field uses the bit `1 << ordinal`.

| Method             | Operation            | Example                                                      |
|--------------------|----------------------|--------------------------------------------------------------|
| `union(other)`     | `bits \| other.bits` | `{title, author} ∪ {author, pages} = {title, author, pages}` |
| `intersect(other)` | `bits & other.bits`  | `{title, author} ∩ {author, pages} = {author}`               |
| `minus(other)`     | `bits & ~other.bits` | `{title, author} − {author, pages} = {title}`                |

`values(book)` returns the masked fields as a map. The service and the REST API use this map.

`format(book)` returns the masked fields as text, for example `{title=Dune, rating=4.5}`.
The static `print(book, mask)` writes the result of `format` to the console.
Tests check `format`, because a string is easier to check than the console output.
Both mask versions have `format` and `print`.

`FieldMask.toString()` shows the field names, for example `FieldMask[title, rating]`.

## Service

`BookService` contains the logic that uses masks:

- `find(title, mask)` returns each book with its `id` and the masked fields.
- `merge(mask)` finds the groups of books that have equal masked fields. In each group, it keeps the
  book with the lowest `id` and deletes the other books.
- `copy(source, match, copy)` finds the books that have the same `match` fields as `source`. It
  copies the `copy` fields from `source` to these books.

`merge` and `copy` load all books into memory. This is sufficient for the lab.

## REST API

```
GET /api/books?title={title}&fields={field,field,...}
```

| Parameter | Required | Description                                                                             |
|-----------|----------|-----------------------------------------------------------------------------------------|
| `title`   | No       | Returns only the books with this title                                                  |
| `fields`  | No       | Returns only these fields. If the request has no `fields`, the response has all fields. |

The field names are case-insensitive. An unknown field name gives the status `400 Bad Request`.

Example request:

```
GET /api/books?title=Dune&fields=title,rating
```

Response:

```json
[
  {
    "id": 1,
    "title": "Dune",
    "rating": 4.5
  }
]
```

Swagger UI is at `/swagger-ui.html`.

## Tests

| Test class           | What it checks                                                                            |
|----------------------|-------------------------------------------------------------------------------------------|
| `BoolFieldMaskTest`  | `has`, conversion to `FieldMask`, `format`                                                |
| `FieldMaskTest`      | `has`, `values`, `union`, `intersect`, `minus`, field copy, `format`, `print`, `toString` |
| `BookServiceTest`    | `findByTitle`, `find`, `merge`, `copy` on an H2 database                                  |
| `BookControllerTest` | Field selection in JSON, status `400` for an unknown field                                |

## How to run

```bash
./gradlew test      # run all tests
./gradlew bootRun   # start the application on port 8080
```

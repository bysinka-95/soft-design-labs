package com.nbugaenco.softdesign.fieldmask.mask;

import com.nbugaenco.softdesign.fieldmask.entity.Book;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * The first version of the field mask. Each field of {@link Book} has one boolean.
 * {@link FieldMask} replaces this record with bits.
 */
public record BoolFieldMask(boolean title, boolean author, boolean pages, boolean rating,
                            boolean genre) {

  /**
   * Writes the masked fields of the book to the console.
   */
  public static void print(Book book, BoolFieldMask mask) {
    System.out.println(mask.format(book));
  }

  public boolean has(BookField field) {
    return switch (field) {
      case TITLE -> title;
      case AUTHOR -> author;
      case PAGES -> pages;
      case RATING -> rating;
      case GENRE -> genre;
    };
  }

  /**
   * Returns a {@link FieldMask} with the same fields.
   */
  public FieldMask toFieldMask() {
    return FieldMask.of(Arrays.stream(BookField.values()).filter(this::has).toList());
  }

  /**
   * Returns the masked fields of the book as text, for example {@code {title=Dune, rating=4.5}}.
   */
  public String format(Book book) {
    return Arrays.stream(BookField.values())
        .filter(this::has)
        .map(field -> field.key() + "=" + field.get(book))
        .collect(Collectors.joining(", ", "{", "}"));
  }
}

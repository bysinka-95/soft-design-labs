package com.nbugaenco.softdesign.fieldmask.mask;

import com.nbugaenco.softdesign.fieldmask.entity.Book;
import com.nbugaenco.softdesign.fieldmask.entity.Genre;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A field of {@link Book} that a {@link FieldMask} can select.
 */
public enum BookField {
  TITLE(Book::getTitle, (book, value) -> book.setTitle((String) value)),
  AUTHOR(Book::getAuthor, (book, value) -> book.setAuthor((String) value)),
  PAGES(Book::getPages, (book, value) -> book.setPages((Integer) value)),
  RATING(Book::getRating, (book, value) -> book.setRating((Float) value)),
  GENRE(Book::getGenre, (book, value) -> book.setGenre((Genre) value));

  private final Function<Book, Object> getter;
  private final BiConsumer<Book, Object> setter;

  BookField(Function<Book, Object> getter, BiConsumer<Book, Object> setter) {
    this.getter = getter;
    this.setter = setter;
  }

  /**
   * Returns the value of this field in the book.
   */
  public Object get(Book book) {
    return getter.apply(book);
  }

  /**
   * Copies the value of this field from the source book to the target book.
   */
  public void copy(Book source, Book target) {
    setter.accept(target, get(source));
  }

  /**
   * Returns the field name in lowercase, as JSON and console output show it.
   */
  public String key() {
    return name().toLowerCase(Locale.ROOT);
  }

  int bit() {
    return 1 << ordinal();
  }
}

package com.nbugaenco.softdesign.fieldmask.mask;

import com.nbugaenco.softdesign.fieldmask.entity.Book;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;

/**
 * A set of {@link BookField} values. Each field uses one bit of {@code bits}. The object is
 * immutable. The combination methods return a new mask.
 */
public record FieldMask(int bits) {

  public static final FieldMask NONE = new FieldMask(0);
  public static final FieldMask ALL = of(BookField.values());

  public static FieldMask of(BookField... fields) {
    return of(Arrays.asList(fields));
  }

  public static FieldMask of(Collection<BookField> fields) {
    return new FieldMask(fields.stream().mapToInt(BookField::bit).reduce(0, (a, b) -> a | b));
  }

  /**
   * Writes the masked fields of the book to the console.
   */
  public static void print(Book book, FieldMask mask) {
    System.out.println(mask.format(book));
  }

  public boolean has(BookField field) {
    return (bits & field.bit()) != 0;
  }

  /**
   * Returns the fields that are in this mask or in the other mask.
   */
  public FieldMask union(FieldMask other) {
    return new FieldMask(bits | other.bits);
  }

  /**
   * Returns the fields that are in this mask and in the other mask.
   */
  public FieldMask intersect(FieldMask other) {
    return new FieldMask(bits & other.bits);
  }

  /**
   * Returns the fields that are in this mask but not in the other mask.
   */
  public FieldMask minus(FieldMask other) {
    return new FieldMask(bits & ~other.bits);
  }

  /**
   * Returns the fields of this mask in declaration order.
   */
  public Stream<BookField> fields() {
    return Arrays.stream(BookField.values()).filter(this::has);
  }

  /**
   * Returns the values of the masked fields of the book. The map keys are {@link BookField#key()}.
   */
  public Map<String, Object> values(Book book) {
    return fields().collect(Collectors.toMap(BookField::key, field -> field.get(book)));
  }

  /**
   * Returns the masked fields of the book as text, for example {@code {title=Dune, rating=4.5}}.
   */
  public String format(Book book) {
    return values(book).toString();
  }

  /**
   * Returns the field names of this mask, for example {@code FieldMask[title, rating]}.
   */
  @Override
  public @NonNull String toString() {
    return "FieldMask" + fields().map(BookField::key).toList();
  }
}

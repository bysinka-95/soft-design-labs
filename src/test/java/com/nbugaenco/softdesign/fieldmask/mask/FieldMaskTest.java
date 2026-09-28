package com.nbugaenco.softdesign.fieldmask.mask;

import static com.nbugaenco.softdesign.fieldmask.mask.BookField.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.nbugaenco.softdesign.fieldmask.entity.Book;
import com.nbugaenco.softdesign.fieldmask.entity.Genre;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@Slf4j
@ExtendWith(OutputCaptureExtension.class)
class FieldMaskTest {

  private final Book dune = new Book("Dune", "Frank Herbert", 612, 4.5f, Genre.SCIENCE_FICTION);

  @Test
  void hasOnlySelectedFields() {
    FieldMask mask = FieldMask.of(TITLE, RATING);
    log.info("{} has bits {}", mask, Integer.toBinaryString(mask.bits()));

    assertThat(mask.has(TITLE)).isTrue();
    assertThat(mask.has(AUTHOR)).isFalse();
    assertThat(mask.fields()).containsExactly(TITLE, RATING);
  }

  @Test
  void valuesContainOnlyMaskedFields() {
    Map<String, Object> values = FieldMask.of(GENRE, PAGES).values(dune);
    log.info("Values: {}", values);

    assertThat(values)
        .containsExactly(Map.entry("pages", 612), Map.entry("genre", Genre.SCIENCE_FICTION));
    assertThat(FieldMask.NONE.values(dune)).isEmpty();
    assertThat(FieldMask.ALL.values(dune)).hasSameSizeAs(BookField.values());
  }

  @Test
  void combinesMasks() {
    FieldMask a = FieldMask.of(TITLE, AUTHOR);
    FieldMask b = FieldMask.of(AUTHOR, PAGES);
    log.info("a = {}, b = {}", a, b);
    log.info("a union b = {}", a.union(b));
    log.info("a intersect b = {}", a.intersect(b));
    log.info("a minus b = {}", a.minus(b));

    assertThat(a.union(b)).isEqualTo(FieldMask.of(TITLE, AUTHOR, PAGES));
    assertThat(a.intersect(b)).isEqualTo(FieldMask.of(AUTHOR));
    assertThat(a.minus(b)).isEqualTo(FieldMask.of(TITLE));
    assertThat(FieldMask.ALL.minus(FieldMask.ALL)).isEqualTo(FieldMask.NONE);
  }

  @Test
  void copiesMaskedFields() {
    Book target = new Book("Other", "Someone", 1, 1f, Genre.ROMANCE);
    log.info("Before copy: {}", FieldMask.ALL.format(target));

    FieldMask.of(TITLE, GENRE).fields().forEach(field -> field.copy(dune, target));
    log.info("After copy:  {}", FieldMask.ALL.format(target));

    assertThat(target.getTitle()).isEqualTo("Dune");
    assertThat(target.getGenre()).isEqualTo(Genre.SCIENCE_FICTION);
    assertThat(target.getAuthor()).isEqualTo("Someone");
  }

  @Test
  void formatsOnlyMaskedFields() {
    String text = FieldMask.of(TITLE, RATING).format(dune);
    log.info("Formatted book: {}", text);

    assertThat(text).isEqualTo("{title=Dune, rating=4.5}");
    assertThat(FieldMask.NONE.format(dune)).isEqualTo("{}");
  }

  @Test
  void printsFormattedFields(CapturedOutput output) {
    FieldMask.print(dune, FieldMask.of(TITLE, RATING));

    assertThat(output.getOut().strip()).isEqualTo("{title=Dune, rating=4.5}");
  }

  @Test
  void showsFieldNames() {
    FieldMask mask = FieldMask.of(RATING, TITLE);
    log.info("toString: {}", mask);

    assertThat(mask).hasToString("FieldMask[title, rating]");
  }
}

package com.nbugaenco.softdesign.fieldmask.mask;

import static com.nbugaenco.softdesign.fieldmask.mask.BookField.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.nbugaenco.softdesign.fieldmask.entity.Book;
import com.nbugaenco.softdesign.fieldmask.entity.Genre;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class BoolFieldMaskTest {

  private final BoolFieldMask titleAndRating = new BoolFieldMask(true, false, false, true, false);

  @Test
  void hasOnlySelectedFields() {
    log.info("Mask: {}", titleAndRating);

    assertThat(titleAndRating.has(TITLE)).isTrue();
    assertThat(titleAndRating.has(RATING)).isTrue();
    assertThat(titleAndRating.has(AUTHOR)).isFalse();
  }

  @Test
  void convertsToBitMask() {
    FieldMask bits = titleAndRating.toFieldMask();
    log.info("{} -> {} (bits={})", titleAndRating, bits, Integer.toBinaryString(bits.bits()));

    assertThat(bits).isEqualTo(FieldMask.of(TITLE, RATING));
  }

  @Test
  void formatsOnlyMaskedFields() {
    Book book = new Book("Dune", "Frank Herbert", 612, 4.5f, Genre.SCIENCE_FICTION);
    String text = titleAndRating.format(book);
    log.info("Formatted book: {}", text);

    assertThat(text).isEqualTo("{title=Dune, rating=4.5}")
        .isEqualTo(titleAndRating.toFieldMask().format(book));
  }
}

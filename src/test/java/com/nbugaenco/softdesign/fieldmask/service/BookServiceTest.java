package com.nbugaenco.softdesign.fieldmask.service;

import static com.nbugaenco.softdesign.fieldmask.mask.BookField.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.nbugaenco.softdesign.fieldmask.entity.Book;
import com.nbugaenco.softdesign.fieldmask.entity.Genre;
import com.nbugaenco.softdesign.fieldmask.mask.FieldMask;
import com.nbugaenco.softdesign.fieldmask.repository.BookRepository;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@Slf4j
@DataJpaTest
@Import(BookService.class)
class BookServiceTest {

  @Autowired
  private BookRepository books;

  @Autowired
  private BookService service;

  @BeforeEach
  void setUp() {
    books.deleteAll();
    books.saveAll(List.of(
        new Book("Dune", "Frank Herbert", 612, 4.5f, Genre.SCIENCE_FICTION),
        new Book("Dune", "Frank Herbert", 896, 4.1f, Genre.SCIENCE_FICTION),
        new Book("The Hobbit", "J. R. R. Tolkien", 310, 4.7f, Genre.FANTASY),
        new Book("The Silmarillion", "J. R. R. Tolkien", 365, 4.0f, Genre.FANTASY)));
    logBooks("Books before the test");
  }

  private void logBooks(String title) {
    log.info(title);
    books.findAll().forEach(book -> log.info("  {}", FieldMask.ALL.format(book)));
  }

  @Test
  void findsByTitle() {
    List<Book> found = books.findByTitle("Dune");
    log.info("findByTitle(\"Dune\") found {} books", found.size());

    assertThat(found).hasSize(2);
    assertThat(books.findByTitle("Missing")).isEmpty();
  }

  @Test
  void findsMaskedFields() {
    List<Map<String, Object>> found = service.find("The Hobbit", FieldMask.of(PAGES));
    log.info("find(\"The Hobbit\", {}) = {}", FieldMask.of(PAGES), found);

    assertThat(found)
        .singleElement()
        .satisfies(book -> assertThat(book).containsOnlyKeys("id", "pages").containsEntry("pages", 310));
  }

  @Test
  void mergesBooksWithEqualMaskedFields() {
    int deleted = service.merge(FieldMask.of(TITLE, AUTHOR));
    logBooks("After merge by " + FieldMask.of(TITLE, AUTHOR) + ", deleted " + deleted);

    assertThat(deleted).isOne();
    assertThat(books.findByTitle("Dune")).singleElement().extracting(Book::getPages).isEqualTo(612);

    deleted = service.merge(FieldMask.of(GENRE));
    logBooks("After merge by " + FieldMask.of(GENRE) + ", deleted " + deleted);

    assertThat(deleted).isOne();
    assertThat(books.count()).isEqualTo(2);
  }

  @Test
  void copiesFieldsToMatchingBooks() {
    Book source = new Book("Any", "Someone", 0, 5f, Genre.FANTASY);

    int changed = service.copy(source, FieldMask.of(GENRE), FieldMask.of(RATING, AUTHOR));
    logBooks("After copy of " + FieldMask.of(RATING, AUTHOR) + " to books with the same "
        + FieldMask.of(GENRE) + ", changed " + changed);

    assertThat(changed).isEqualTo(2);

    assertThat(books.findAll())
        .filteredOn(book -> book.getGenre() == Genre.FANTASY)
        .allSatisfy(book -> {
          assertThat(book.getRating()).isEqualTo(5f);
          assertThat(book.getAuthor()).isEqualTo("Someone");
        });
    assertThat(books.findByTitle("Dune")).allSatisfy(book -> assertThat(book.getRating()).isLessThan(5f));
  }
}

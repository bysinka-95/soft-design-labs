package com.nbugaenco.softdesign.fieldmask.service;

import com.nbugaenco.softdesign.fieldmask.entity.Book;
import com.nbugaenco.softdesign.fieldmask.mask.FieldMask;
import com.nbugaenco.softdesign.fieldmask.repository.BookRepository;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

  private final BookRepository books;

  /**
   * Returns the books. Each book has its id and the fields of the mask. If the title is not null,
   * the result has only the books with this title.
   */
  public List<Map<String, Object>> find(String title, FieldMask mask) {
    List<Book> found = title == null ? books.findAll() : books.findByTitle(title);
    return found.stream().map(book -> {
      Map<String, Object> result = new LinkedHashMap<>();
      result.put("id", book.getId());
      result.putAll(mask.values(book));
      return result;
    }).toList();
  }

  /**
   * Finds the books that have equal values in all fields of the mask. Keeps the book with the
   * lowest id in each group and deletes the other books.
   *
   * @return the number of deleted books
   */
  @Transactional
  public int merge(FieldMask mask) {
    HashSet<Map<String, Object>> seen = new HashSet<>();
    List<Book> duplicates = books.findAll(Sort.by("id")).stream()
        .filter(book -> !seen.add(mask.values(book)))
        .toList();
    books.deleteAll(duplicates);
    return duplicates.size();
  }

  /**
   * Finds the books that have the same values as the source in all fields of {@code match}. Copies
   * the fields of {@code copy} from the source to these books.
   *
   * @return the number of changed books
   */
  @Transactional
  public int copy(Book source, FieldMask match, FieldMask copy) {
    Map<String, Object> key = match.values(source);
    List<Book> targets = books.findAll().stream()
        .filter(book -> key.equals(match.values(book)))
        .toList();
    targets.forEach(book -> copy.fields().forEach(field -> field.copy(source, book)));
    return targets.size();
  }
}

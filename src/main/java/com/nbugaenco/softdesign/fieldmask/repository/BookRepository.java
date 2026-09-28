package com.nbugaenco.softdesign.fieldmask.repository;

import com.nbugaenco.softdesign.fieldmask.entity.Book;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

  List<Book> findByTitle(String title);
}

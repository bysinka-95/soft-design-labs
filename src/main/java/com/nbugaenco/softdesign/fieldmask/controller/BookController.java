package com.nbugaenco.softdesign.fieldmask.controller;

import com.nbugaenco.softdesign.fieldmask.mask.BookField;
import com.nbugaenco.softdesign.fieldmask.mask.FieldMask;
import com.nbugaenco.softdesign.fieldmask.service.BookService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
class BookController {

  private final BookService service;

  /**
   * Returns the books with the fields in {@code fields}. If the request has no fields, the books
   * have all fields.
   */
  @GetMapping
  List<Map<String, Object>> find(
      @RequestParam(required = false) String title,
      @RequestParam(required = false) Set<BookField> fields) {
    return service.find(title, fields == null ? FieldMask.ALL : FieldMask.of(fields));
  }
}

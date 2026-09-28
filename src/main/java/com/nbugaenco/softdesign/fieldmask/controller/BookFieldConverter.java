package com.nbugaenco.softdesign.fieldmask.controller;

import com.nbugaenco.softdesign.fieldmask.mask.BookField;
import java.util.Locale;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converts a request value such as {@code "rating"} to a {@link BookField}.
 */
@Component
class BookFieldConverter implements Converter<String, BookField> {

  @Override
  public BookField convert(String source) {
    return BookField.valueOf(source.trim().toUpperCase(Locale.ROOT));
  }
}

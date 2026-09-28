package com.nbugaenco.softdesign.fieldmask.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;
  private String author;
  private int pages;
  private float rating;

  @Enumerated(EnumType.STRING)
  private Genre genre;

  public Book(String title, String author, int pages, float rating, Genre genre) {
    this.title = title;
    this.author = author;
    this.pages = pages;
    this.rating = rating;
    this.genre = genre;
  }
}

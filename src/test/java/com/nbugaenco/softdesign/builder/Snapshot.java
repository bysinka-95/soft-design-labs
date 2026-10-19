package com.nbugaenco.softdesign.builder;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Minimal snapshot testing. The first run writes the snapshot file, the next runs compare with it.
 * To accept a new result, delete the file and run the test again.
 */
final class Snapshot {

  private static final Path DIR = Path.of("src/test/resources/snapshots");

  private Snapshot() {
  }

  static void assertMatches(String name, String actual) throws IOException {
    Path file = DIR.resolve(name + ".txt");
    if (Files.notExists(file)) {
      Files.createDirectories(DIR);
      Files.writeString(file, actual);
    }
    assertThat(actual).isEqualTo(Files.readString(file));
  }
}

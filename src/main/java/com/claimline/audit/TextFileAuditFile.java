package com.claimline.audit;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/** An {@link AuditFile} stored as one entry per line, with fields separated by tabs. */
public final class TextFileAuditFile implements AuditFile {

  private static final String SEPARATOR = "\t";
  private static final int FIELD_COUNT = 6;

  private final Path file;

  public TextFileAuditFile(Path file) {
    this.file = file;
  }

  @Override
  public void append(AuditEntry entry) {
    // Fields are written in a fixed order.
    String line =
        entry.timestamp()
            + SEPARATOR
            + entry.action()
            + SEPARATOR
            + entry.claimId()
            + SEPARATOR
            + entry.amount()
            + SEPARATOR
            + entry.category()
            + SEPARATOR
            + entry.approverId()
            + System.lineSeparator();
    try {
      Files.createDirectories(file.toAbsolutePath().getParent());
      Files.writeString(
          file, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    } catch (IOException e) {
      throw new UncheckedIOException("could not append to the audit file", e);
    }
  }

  @Override
  public List<AuditEntry> readAll() {
    if (!Files.exists(file)) {
      return List.of();
    }
    List<AuditEntry> entries = new ArrayList<>();
    try {
      for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
        if (line.isBlank()) {
          continue;
        }
        AuditEntry entry = parse(line);
        if (entry != null) {
          entries.add(entry);
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException("could not read the audit file", e);
    }
    return entries;
  }

  /** Reads a single line, returning null if it does not match the expected fields. */
  static AuditEntry parse(String line) {
    String[] fields = line.split(SEPARATOR, -1);
    if (fields.length != FIELD_COUNT) {
      return null;
    }
    long amount;
    try {
      amount = Long.parseLong(fields[3]);
    } catch (NumberFormatException e) {
      return null;
    }
    return new AuditEntry(fields[0], fields[1], fields[2], amount, fields[4], fields[5]);
  }
}

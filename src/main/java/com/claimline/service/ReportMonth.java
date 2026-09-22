package com.claimline.service;

import java.util.regex.Pattern;

/** A calendar month to report on, formatted {@code YYYY-MM}. */
public record ReportMonth(String value) {

  private static final Pattern FORMAT = Pattern.compile("\\d{4}-(0[1-9]|1[0-2])");

  public ReportMonth {
    if (value == null || !FORMAT.matcher(value).matches()) {
      throw new IllegalArgumentException("month must be formatted YYYY-MM, for example 2026-04");
    }
  }

  /** Whether an audit entry timestamp falls in this month. */
  public boolean covers(String timestamp) {
    return timestamp.startsWith(value);
  }
}

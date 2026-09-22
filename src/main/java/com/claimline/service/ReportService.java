package com.claimline.service;

import com.claimline.audit.AuditEntry;
import com.claimline.audit.AuditFile;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/** Builds the monthly spend report that finance reconciles against. */
public final class ReportService {

  private final AuditFile auditFile;

  public ReportService(AuditFile auditFile) {
    this.auditFile = auditFile;
  }

  /** Totals the approved spend of each category for the given month. */
  public MonthlyReport monthly(ReportMonth month) {
    Map<String, Long> totals = new TreeMap<>();
    long total = 0;
    for (AuditEntry entry : auditFile.readAll()) {
      if (!AuditEntry.APPROVED.equals(entry.action()) || !month.covers(entry.timestamp())) {
        continue;
      }
      totals.merge(entry.category(), entry.amount(), Long::sum);
      total += entry.amount();
    }
    return new MonthlyReport(month.value(), new LinkedHashMap<>(totals), total);
  }
}

package com.claimline.audit;

import java.util.List;

/** An append-only record of every change to a claim. */
public interface AuditFile {

  void append(AuditEntry entry);

  /** Every entry that could be read back. */
  List<AuditEntry> readAll();
}

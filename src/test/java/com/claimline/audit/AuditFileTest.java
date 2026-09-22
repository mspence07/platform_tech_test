package com.claimline.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AuditFileTest {

  @TempDir Path tempDir;

  @Test
  void appendedEntriesReadBackInOrder() {
    AuditFile file = new TextFileAuditFile(tempDir.resolve("audit-log.txt"));
    file.append(AuditEntry.submitted("2026-07-01T09:00:00Z", "clm-1", 10, "meals"));
    file.append(AuditEntry.approved("2026-07-01T10:00:00Z", "clm-1", 10, "meals", "alice"));

    List<AuditEntry> entries = file.readAll();

    assertEquals(2, entries.size());
    assertEquals(AuditEntry.SUBMITTED, entries.get(0).action());
    assertEquals(AuditEntry.APPROVED, entries.get(1).action());
    assertEquals("alice", entries.get(1).approverId());
    assertEquals(10, entries.get(1).amount());
  }

  @Test
  void parseReturnsNullForALineThatDoesNotMatchTheFields() {
    assertNull(TextFileAuditFile.parse("2026-07-01T09:00:00Z\tapproved\tclm-1"));
  }
}

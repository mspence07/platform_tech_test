package com.claimline.support;

import com.claimline.audit.AuditFile;
import com.claimline.audit.TextFileAuditFile;
import com.claimline.policy.ApprovalPolicy;
import com.claimline.policy.ApprovalRequirements;
import com.claimline.policy.Threshold;
import com.claimline.service.ClaimService;
import com.claimline.service.ReportService;
import com.claimline.service.SubmitClaimRequest;
import com.claimline.store.InMemoryClaimStore;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Builds wired services for tests. Amounts are whole dollars. */
public final class TestServices {

  /** The largest amount a claim may be submitted for. */
  public static final long MAX_CLAIM = 20_000;

  /** A pinned timestamp, so that audit entries land in a known month. */
  public static final String NOW = "2026-07-15T10:00:00Z";

  private TestServices() {}

  public static ApprovalPolicy policy() {
    return new ApprovalPolicy(
        Map.of(
            "alice", 500L,
            "bharat", 5_000L,
            "chen", 10_000L,
            "dana", 5_000L,
            "eshe", 20_000L,
            "farouk", 20_000L));
  }

  public static ApprovalRequirements approvalRequirements() {
    return new ApprovalRequirements(
        List.of(new Threshold(0, 1), new Threshold(1_000, 2), new Threshold(10_000, 3)));
  }

  public static AuditFile auditFile(Path file) {
    return new TextFileAuditFile(file);
  }

  public static ClaimService claimService(Path auditFile) {
    return new ClaimService(
        new InMemoryClaimStore(),
        policy(),
        approvalRequirements(),
        auditFile(auditFile),
        () -> NOW);
  }

  public static ReportService reportService(Path auditFile) {
    return new ReportService(auditFile(auditFile));
  }

  public static SubmitClaimRequest claimFor(long amount, String category) {
    return new SubmitClaimRequest("erin", amount, category, "a claim");
  }
}

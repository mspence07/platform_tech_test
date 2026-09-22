package com.claimline.service;

import com.claimline.audit.AuditEntry;
import com.claimline.audit.AuditFile;
import com.claimline.policy.ApprovalPolicy;
import com.claimline.policy.ApprovalRequirements;
import com.claimline.store.Approval;
import com.claimline.store.Claim;
import com.claimline.store.ClaimStore;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Submits, approves, and reads expense claims. */
public final class ClaimService {

  private final ClaimStore claims;
  private final ApprovalPolicy approvalPolicy;
  private final ApprovalRequirements approvalRequirements;
  private final AuditFile auditFile;
  private final Clock clock;

  public ClaimService(
      ClaimStore claims,
      ApprovalPolicy approvalPolicy,
      ApprovalRequirements approvalRequirements,
      AuditFile auditFile,
      Clock clock) {
    this.claims = claims;
    this.approvalPolicy = approvalPolicy;
    this.approvalRequirements = approvalRequirements;
    this.auditFile = auditFile;
    this.clock = clock;
  }

  public Claim submit(SubmitClaimRequest request) {
    if (request.submitterId() == null || request.submitterId().isBlank()) {
      throw new IllegalArgumentException("submitterId is required");
    }
    if (request.category() == null || request.category().isBlank()) {
      throw new IllegalArgumentException("category is required");
    }
    if (request.amount() <= 0) {
      throw new IllegalArgumentException("amount must be positive");
    }
    if (request.amount() > 20_000) {
      throw new IllegalArgumentException("amount exceeds the maximum claimable amount");
    }

    final int approvalsRequired = approvalRequirements.approvalsRequiredFor(request.amount());

    Claim claim =
        new Claim(
            "clm-" + UUID.randomUUID().toString().substring(0, 8),
            request.submitterId(),
            request.amount(),
            request.category(),
            Claim.PENDING,
            null,
            approvalsRequired,
            List.of());
    claims.save(claim);
    auditFile.append(
        AuditEntry.submitted(clock.nowIso(), claim.id(), claim.amount(), claim.category()));
    return claim;
  }

  public Claim approve(String claimId, String approverId) {
    Claim claim =
        claims.findById(claimId).orElseThrow(() -> new ClaimNotFoundException("unknown claim"));

    if (Claim.APPROVED.equals(claim.status())) {
      throw new ApprovalDeniedException("claim is already approved!");
    }

    if (!approvalPolicy.check(approverId, claim.amount())) {
      throw new ApprovalDeniedException("approver is not authorised for this amount");
    }

    for (Approval existingApproval : claim.approvals()) {
      if (existingApproval.approverId().equals(approverId)) {
        throw new ApprovalDeniedException("approver has already approved this claim!");
      }
    }

    final String approvedAt = clock.nowIso();

    List<Approval> updatedApprovals = new ArrayList<>(claim.approvals());

    updatedApprovals.add(new Approval(approverId, approvedAt));

    final boolean fullyApproved = updatedApprovals.size() >= claim.approvalsRequired();

    Claim updatedClaim;

    if (fullyApproved) {
      updatedClaim = claim.approvedBy(approverId, updatedApprovals);
    } else {
      updatedClaim = claim.withApprovals(updatedApprovals);
    }

    claims.save(updatedClaim);

    if (fullyApproved) {
      auditFile.append(
          AuditEntry.approved(
              approvedAt,
              updatedClaim.id(),
              updatedClaim.amount(),
              updatedClaim.category(),
              approverId));
    } else {
      auditFile.append(
          AuditEntry.approvalRecorded(
              approvedAt,
              updatedClaim.id(),
              updatedClaim.amount(),
              updatedClaim.category(),
              approverId));
    }

    return updatedClaim;
  }

  public Claim get(String claimId) {
    return claims.findById(claimId).orElseThrow(() -> new ClaimNotFoundException("unknown claim"));
  }
}

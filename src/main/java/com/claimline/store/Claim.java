package com.claimline.store;

import java.util.List;

/** An expense claim submitted by an employee. Amounts are whole dollars. */
public record Claim(
    String id,
    String submitterId,
    long amount,
    String category,
    String status,
    String approvedBy,
    int approvalsRequired,
    List<Approval> approvals) {

  public static final String PENDING = "pending";
  public static final String APPROVED = "approved";

  public Claim withApprovals(List<Approval> updatedApprovals) {
    return new Claim(
        id, submitterId, amount, category, status, approvedBy, approvalsRequired, updatedApprovals);
  }

  public Claim approvedBy(String approverId, List<Approval> approvals) {
    return new Claim(
        id, submitterId, amount, category, APPROVED, approverId, approvalsRequired, approvals);
  }
}

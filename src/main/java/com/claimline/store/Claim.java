package com.claimline.store;

/** An expense claim submitted by an employee. Amounts are whole dollars. */
public record Claim(
    String id,
    String submitterId,
    long amount,
    String category,
    String status,
    String approvedBy) {

  public static final String PENDING = "pending";
  public static final String APPROVED = "approved";

  public Claim approvedBy(String approverId) {
    return new Claim(id, submitterId, amount, category, APPROVED, approverId);
  }
}

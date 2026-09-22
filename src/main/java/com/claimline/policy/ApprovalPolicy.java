package com.claimline.policy;

import java.util.Map;

/** Decides whether an approver is authorised to approve a claim of a given size. */
public final class ApprovalPolicy {

  private final Map<String, Long> limitByApprover;

  public ApprovalPolicy(Map<String, Long> limitByApprover) {
    this.limitByApprover = Map.copyOf(limitByApprover);
  }

  public boolean check(String approverId, long amount) {
    Long limit = limitByApprover.get(approverId);
    return limit != null && amount <= limit;
  }
}

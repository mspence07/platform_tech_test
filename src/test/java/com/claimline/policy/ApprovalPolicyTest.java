package com.claimline.policy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ApprovalPolicyTest {

  private final ApprovalPolicy policy = new ApprovalPolicy(Map.of("alice", 500L));

  @Test
  void allowsAnAmountUpToTheApproverLimit() {
    assertTrue(policy.check("alice", 100));
    assertTrue(policy.check("alice", 500));
  }

  @Test
  void rejectsAnAmountOverTheApproverLimit() {
    assertFalse(policy.check("alice", 501));
  }

  @Test
  void rejectsAnUnknownApprover() {
    assertFalse(policy.check("nobody", 1));
  }
}

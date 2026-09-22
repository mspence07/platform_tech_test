package com.claimline.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.claimline.store.Claim;
import com.claimline.support.TestServices;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClaimServiceTest {

  @TempDir Path tempDir;

  private ClaimService service;

  @BeforeEach
  void setUp() {
    service = TestServices.claimService(tempDir.resolve("audit-log.txt"));
  }

  @Test
  void submittingAClaimStartsItPending() {
    Claim claim = service.submit(TestServices.claimFor(42, "travel"));

    assertNotNull(claim.id());
    assertEquals(Claim.PENDING, claim.status());
    assertNull(claim.approvedBy());
  }

  @Test
  void rejectsAClaimWithANonPositiveAmount() {
    assertThrows(
        IllegalArgumentException.class, () -> service.submit(TestServices.claimFor(0, "travel")));
  }

  @Test
  void rejectsAClaimOverTheMaximumAmount() {
    assertThrows(
        IllegalArgumentException.class,
        () -> service.submit(TestServices.claimFor(TestServices.MAX_CLAIM + 1, "equipment")));
  }

  @Test
  void approvingAClaimRecordsTheApprover() {
    Claim claim = service.submit(TestServices.claimFor(315, "travel"));

    Claim approved = service.approve(claim.id(), "bharat");

    assertEquals(Claim.APPROVED, approved.status());
    assertEquals("bharat", approved.approvedBy());
  }

  @Test
  void rejectsAnApproverWhoseLimitIsTooLow() {
    Claim claim = service.submit(TestServices.claimFor(1_289, "equipment"));

    assertThrows(ApprovalDeniedException.class, () -> service.approve(claim.id(), "alice"));
  }

  @Test
  void rejectsAnUnknownClaim() {
    assertThrows(ClaimNotFoundException.class, () -> service.approve("clm-missing", "chen"));
  }
}

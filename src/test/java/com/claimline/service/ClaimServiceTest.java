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

  @Test
  void testApprovalsRequiredFromClaimAmounts() {
    Claim smallClaim = service.submit(TestServices.claimFor(999, "travel"));

    Claim mediumClaim = service.submit(TestServices.claimFor(1_000, "meals"));

    Claim largestMediumClaim = service.submit(TestServices.claimFor(9_999, "travel"));

    Claim largeClaim = service.submit(TestServices.claimFor(10_000, "equipment"));

    assertEquals(1, smallClaim.approvalsRequired());
    assertEquals(2, mediumClaim.approvalsRequired());
    assertEquals(2, largestMediumClaim.approvalsRequired());
    assertEquals(3, largeClaim.approvalsRequired());
  }

  @Test
  void testClaimIsPendingUntilItGetsEnoughApprovals() {
    Claim submitted = service.submit(TestServices.claimFor(1_289, "equipment"));

    Claim afterFirstApproval = service.approve(submitted.id(), "bharat");

    assertEquals(Claim.PENDING, afterFirstApproval.status());
    assertNull(afterFirstApproval.approvedBy());
    assertEquals(1, afterFirstApproval.approvals().size());
    assertEquals("bharat", afterFirstApproval.approvals().get(0).approverId());
    assertEquals(TestServices.NOW, afterFirstApproval.approvals().get(0).approvedAt());

    assertEquals(afterFirstApproval, service.get(submitted.id()));

    Claim afterSecondApproval = service.approve(submitted.id(), "chen");

    assertEquals(Claim.APPROVED, afterSecondApproval.status());
    assertEquals("chen", afterSecondApproval.approvedBy());
    assertEquals(2, afterSecondApproval.approvals().size());

    assertEquals(afterSecondApproval, service.get(submitted.id()));
  }

  @Test
  void testSameApproverCannotApprovePendingClaimTwice() {
    Claim submitted = service.submit(TestServices.claimFor(1_289, "equipment"));

    service.approve(submitted.id(), "bharat");

    assertThrows(ApprovalDeniedException.class, () -> service.approve(submitted.id(), "bharat"));

    Claim storedClaim = service.get(submitted.id());

    assertEquals(Claim.PENDING, storedClaim.status());
    assertEquals(1, storedClaim.approvals().size());
    assertEquals("bharat", storedClaim.approvals().get(0).approverId());
  }

  @Test
  void testFullyApprovedClaimCannotBeApprovedAgain() {
    Claim submitted = service.submit(TestServices.claimFor(315, "travel"));

    Claim approved = service.approve(submitted.id(), "bharat");

    assertThrows(ApprovalDeniedException.class, () -> service.approve(submitted.id(), "bharat"));

    Claim storedClaim = service.get(submitted.id());

    assertEquals(Claim.APPROVED, storedClaim.status());
    assertEquals(approved, storedClaim);
    assertEquals(1, storedClaim.approvals().size());
  }

  @Test
  void testLargeClaimRequiresThreeApprovals() {
    Claim submitted = service.submit(TestServices.claimFor(10_000, "equipment"));

    Claim afterFirst = service.approve(submitted.id(), "chen");

    assertEquals(Claim.PENDING, afterFirst.status());
    assertEquals(1, afterFirst.approvals().size());

    Claim afterSecond = service.approve(submitted.id(), "eshe");

    assertEquals(Claim.PENDING, afterSecond.status());
    assertEquals(2, afterSecond.approvals().size());

    Claim afterThird = service.approve(submitted.id(), "farouk");

    assertEquals(Claim.APPROVED, afterThird.status());
    assertEquals(3, afterThird.approvals().size());
  }
}

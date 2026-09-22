package com.claimline.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.claimline.store.Approval;
import com.claimline.store.Claim;
import com.claimline.support.TestServices;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClaimDetailTest {

  @TempDir Path tempDir;

  @Test
  void anApprovedClaimReadsBackWithAllOfItsDetail() {
    ClaimService service = TestServices.claimService(tempDir.resolve("audit-log.txt"));
    Claim submitted = service.submit(TestServices.claimFor(315, "travel"));
    service.approve(submitted.id(), "bharat");

    Claim claim = service.get(submitted.id());

    assertEquals(
        new Claim(
            submitted.id(),
            "erin",
            315,
            "travel",
            Claim.APPROVED,
            "bharat",
            1,
            List.of(new Approval("bharat", TestServices.NOW))),
        claim);
  }
}

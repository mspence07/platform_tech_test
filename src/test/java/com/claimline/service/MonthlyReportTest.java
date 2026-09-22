package com.claimline.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.claimline.store.Claim;
import com.claimline.support.TestServices;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MonthlyReportTest {

  @TempDir Path tempDir;

  /**
   * A few audit entries the service wrote in April and May 2026, in the format it wrote them in.
   * April and May are closed months, so the report has to keep reading lines like these and
   * totalling them the same way.
   *
   * <p>These are a sample rather than the whole of either month, so the totals below are not the
   * ones the live audit file gives. Do not rewrite these lines to suit a change.
   */
  private static final String ENTRIES_ALREADY_RECORDED =
      """
      2026-04-03T09:14:22Z\tsubmitted\tclm-4a1c9e02\t42\ttravel\t
      2026-04-03T11:02:47Z\tapproved\tclm-4a1c9e02\t42\ttravel\talice
      2026-04-07T14:31:08Z\tsubmitted\tclm-5b7d1f33\t1289\tequipment\t
      2026-04-08T08:45:12Z\tapproved\tclm-5b7d1f33\t1289\tequipment\tbharat
      2026-04-11T16:20:55Z\tsubmitted\tclm-6c2e8a41\t315\ttravel\t
      2026-04-12T09:03:19Z\tapproved\tclm-6c2e8a41\t315\ttravel\talice
      2026-04-18T10:47:33Z\tsubmitted\tclm-7d9f2b58\t78\tmeals\t
      2026-04-18T13:15:41Z\tapproved\tclm-7d9f2b58\t78\tmeals\talice
      2026-05-05T08:22:14Z\tsubmitted\tclm-9f4b5d76\t186\ttravel\t
      2026-05-05T15:41:09Z\tapproved\tclm-9f4b5d76\t186\ttravel\talice
      """;

  @Test
  void totalsTheApprovedSpendOfEachCategoryForTheMonth() throws IOException {
    Path auditFile = writeEntriesAlreadyRecorded();

    MonthlyReport report =
        TestServices.reportService(auditFile).monthly(new ReportMonth("2026-04"));

    assertEquals(
        Map.of("travel", 357L, "equipment", 1_289L, "meals", 78L), report.totalsByCategory());
    assertEquals(1_724, report.total());
  }

  @Test
  void countsOnlyTheMonthAskedFor() throws IOException {
    Path auditFile = writeEntriesAlreadyRecorded();

    MonthlyReport report =
        TestServices.reportService(auditFile).monthly(new ReportMonth("2026-05"));

    assertEquals(Map.of("travel", 186L), report.totalsByCategory());
  }

  @Test
  void includesClaimsApprovedThroughTheService() throws IOException {
    Path auditFile = writeEntriesAlreadyRecorded();
    ClaimService claims = TestServices.claimService(auditFile);
    Claim claim = claims.submit(TestServices.claimFor(99, "meals"));
    claims.approve(claim.id(), "bharat");

    MonthlyReport report =
        TestServices.reportService(auditFile).monthly(new ReportMonth("2026-07"));

    assertEquals(Map.of("meals", 99L), report.totalsByCategory());
  }

  @Test
  void testReportsMultiApprovalClaimOnlyWhenFullyApproved() {
    Path auditFile = tempDir.resolve("audit-log.txt");

    ClaimService claims = TestServices.claimService(auditFile);

    Claim claim = claims.submit(TestServices.claimFor(1_289, "equipment"));

    claims.approve(claim.id(), "bharat");

    MonthlyReport partialReport =
        TestServices.reportService(auditFile).monthly(new ReportMonth("2026-07"));

    assertEquals(0, partialReport.total());
    assertEquals(Map.of(), partialReport.totalsByCategory());

    claims.approve(claim.id(), "chen");

    MonthlyReport completedReport =
        TestServices.reportService(auditFile).monthly(new ReportMonth("2026-07"));

    assertEquals(1_289, completedReport.total());
    assertEquals(Map.of("equipment", 1_289L), completedReport.totalsByCategory());
  }

  private Path writeEntriesAlreadyRecorded() throws IOException {
    Path file = tempDir.resolve("audit-log.txt");
    Files.writeString(file, ENTRIES_ALREADY_RECORDED, StandardCharsets.UTF_8);
    return file;
  }
}

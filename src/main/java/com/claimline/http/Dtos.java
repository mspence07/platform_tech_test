package com.claimline.http;

import com.claimline.store.Approval;
import java.util.List;
import java.util.Map;

/** Request and response bodies exchanged over HTTP. */
final class Dtos {

  private Dtos() {}

  record SubmitRequest(String submitterId, long amount, String category, String description) {}

  record ApproveRequest(String approverId) {}

  record ClaimResponse(
      String id,
      String submitterId,
      long amount,
      String category,
      String status,
      String approvedBy,
      int approvalsRequired,
      List<Approval> approvals) {}

  record ReportResponse(String month, Map<String, Long> totalsByCategory, long total) {}
}

package com.claimline.http;

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
      String approvedBy) {}

  record ReportResponse(String month, Map<String, Long> totalsByCategory, long total) {}
}

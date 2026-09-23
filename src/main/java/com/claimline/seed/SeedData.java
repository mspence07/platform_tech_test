package com.claimline.seed;

import com.claimline.policy.ApprovalPolicy;
import com.claimline.store.Claim;
import com.claimline.store.ClaimStore;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Loads the approvers and the claims the service already holds. */
public final class SeedData {

  private static final String APPROVERS = "/com/claimline/seed/approvers.json";
  private static final String CLAIMS = "/com/claimline/seed/claims.json";

  private final List<SeedApprover> approvers;
  private final List<SeedClaim> claims;

  private SeedData(List<SeedApprover> approvers, List<SeedClaim> claims) {
    this.approvers = approvers;
    this.claims = claims;
  }

  public static SeedData load() {

    ApproverFixture approverFixture = read(APPROVERS, ApproverFixture.class);

    ClaimFixture claimFixture = read(CLAIMS, ClaimFixture.class);

    validateClaims(claimFixture.claims);

    return new SeedData(approverFixture.approvers, claimFixture.claims);
  }

  public ApprovalPolicy toApprovalPolicy() {
    Map<String, Long> limits = new HashMap<>();
    for (SeedApprover approver : approvers) {
      limits.put(approver.approverId, approver.limit);
    }
    return new ApprovalPolicy(limits);
  }

  public void seedInto(ClaimStore store) {
    for (SeedClaim claim : claims) {
      store.save(
          new Claim(
              claim.id,
              claim.submitterId,
              claim.amount,
              claim.category,
              claim.status,
              claim.approvedBy,
              1,
              List.of()));
    }
  }

  private static <T> T read(String resource, Class<T> type) {
    try (InputStream in = SeedData.class.getResourceAsStream(resource)) {
      if (in == null) {
        throw new IllegalStateException("fixture not found: " + resource);
      }
      return new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), type);
    } catch (IOException e) {
      throw new IllegalStateException("failed to load fixture: " + resource, e);
    }
  }

  private static final class ApproverFixture {
    List<SeedApprover> approvers;
  }

  private static final class ClaimFixture {
    List<SeedClaim> claims;
  }

  private static final class SeedApprover {
    String approverId;
    String name;
    long limit;
  }

  private static final class SeedClaim {
    String id;
    String submitterId;
    long amount;
    String category;
    String status;
    String approvedBy;
  }

  private static void validateClaims(List<SeedClaim> claims) {
    for (SeedClaim claim : claims) {
      if (claim == null) {
        throw new IllegalStateException("seeded claims cannot contain null");
      }

      validateField("id", claim.id);
      validateField("submitterId", claim.submitterId);
      validateField("category", claim.category);
      validateField("status", claim.status);

      if (claim.amount <= 0 || claim.amount > 20_000) {
        throw new IllegalStateException("invalid amount in seeded claim " + claim.id);
      }

      if (!Claim.PENDING.equals(claim.status) && !Claim.APPROVED.equals(claim.status)) {
        throw new IllegalStateException("invalid status in seeded claim " + claim.id);
      }

      if (Claim.APPROVED.equals(claim.status)) {
        validateField("approvedBy", claim.approvedBy);
      }
    }
  }

  private static void validateField(String fieldName, String value) {
    if (value == null) {
      throw new IllegalArgumentException(fieldName + " cannot be null");
    }

    if (value.contains("\n") || value.contains("\r") || value.contains("\t")) {
      throw new IllegalArgumentException(fieldName + " cannot contain tabs or newlines");
    }
  }
}

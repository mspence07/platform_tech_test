package com.claimline.service;

/** A request to submit a new expense claim. The amount is in whole dollars. */
public record SubmitClaimRequest(
    String submitterId, long amount, String category, String description) {}

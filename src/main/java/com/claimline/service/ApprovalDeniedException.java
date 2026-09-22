package com.claimline.service;

/** Raised when an approver is not authorised to approve a claim. */
public class ApprovalDeniedException extends RuntimeException {

  public ApprovalDeniedException(String message) {
    super(message);
  }
}

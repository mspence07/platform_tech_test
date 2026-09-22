package com.claimline.service;

/** Raised when a claim id does not match a stored claim. */
public class ClaimNotFoundException extends RuntimeException {

  public ClaimNotFoundException(String message) {
    super(message);
  }
}

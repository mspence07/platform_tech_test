package com.claimline.http;

/** Raised by handlers when a request is missing required fields or is malformed. */
class BadRequestException extends RuntimeException {

  BadRequestException(String message) {
    super(message);
  }
}

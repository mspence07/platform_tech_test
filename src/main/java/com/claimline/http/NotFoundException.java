package com.claimline.http;

/** Raised by handlers when a request does not match any route. */
class NotFoundException extends RuntimeException {

  NotFoundException(String message) {
    super(message);
  }
}

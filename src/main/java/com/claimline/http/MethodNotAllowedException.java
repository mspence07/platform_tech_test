package com.claimline.http;

/** Raised by handlers when a route exists but not for the request's method. */
class MethodNotAllowedException extends RuntimeException {

  MethodNotAllowedException(String message) {
    super(message);
  }
}

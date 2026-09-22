package com.claimline.service;

import java.time.Instant;

/** Supplies the current time, so that tests can pin it. */
public interface Clock {

  String nowIso();

  static Clock system() {
    return () -> Instant.now().toString();
  }
}

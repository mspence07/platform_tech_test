package com.claimline.store;

import java.util.Optional;

/** Stores expense claims. */
public interface ClaimStore {

  void save(Claim claim);

  Optional<Claim> findById(String id);
}

package com.claimline.store;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** In-memory {@link ClaimStore} backed by a concurrent map. */
public final class InMemoryClaimStore implements ClaimStore {

  private final ConcurrentMap<String, Claim> claims = new ConcurrentHashMap<>();

  @Override
  public void save(Claim claim) {
    claims.put(claim.id(), claim);
  }

  @Override
  public Optional<Claim> findById(String id) {
    return Optional.ofNullable(claims.get(id));
  }
}

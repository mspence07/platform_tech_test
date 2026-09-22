package com.claimline.config;

import java.nio.file.Path;

/** Runtime configuration for the service. */
public record Config(int port, Path auditFile) {

  private static final Path AUDIT_FILE = Path.of("data/audit-log.txt");

  public static Config fromEnvironment() {
    String port = System.getenv("CLAIMLINE_PORT");
    return new Config(
        port == null || port.isBlank() ? 8080 : Integer.parseInt(port), AUDIT_FILE);
  }
}

package com.claimline;

import com.claimline.audit.AuditFile;
import com.claimline.audit.TextFileAuditFile;
import com.claimline.config.Config;
import com.claimline.http.ClaimApi;
import com.claimline.policy.ApprovalPolicy;
import com.claimline.policy.ApprovalRequirements;
import com.claimline.seed.SeedData;
import com.claimline.service.ClaimService;
import com.claimline.service.Clock;
import com.claimline.service.ReportService;
import com.claimline.store.ClaimStore;
import com.claimline.store.InMemoryClaimStore;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

/** Wires the layers together and starts the HTTP server. */
public final class App {

  public static void main(String[] args) throws IOException {
    Config config = Config.fromEnvironment();

    SeedData seed = SeedData.load();
    ApprovalPolicy approvalPolicy = seed.toApprovalPolicy();
    ClaimStore claims = new InMemoryClaimStore();
    seed.seedInto(claims);
    AuditFile auditFile = new TextFileAuditFile(config.auditFile());

    ApprovalRequirements requirements = ApprovalRequirements.fromJson(config.financeThresholds());

    ClaimService claimService =
        new ClaimService(claims, approvalPolicy, requirements, auditFile, Clock.system());
    ReportService reportService = new ReportService(auditFile);

    HttpServer server = HttpServer.create(new InetSocketAddress(config.port()), 0);
    new ClaimApi(claimService, reportService).register(server);
    server.start();
    System.out.println("claimline listening on http://localhost:" + config.port());
    System.out.println("audit file: " + config.auditFile().toAbsolutePath());
  }

  private App() {}
}

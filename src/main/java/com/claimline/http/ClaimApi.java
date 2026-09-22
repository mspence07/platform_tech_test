package com.claimline.http;

import com.claimline.http.Dtos.ApproveRequest;
import com.claimline.http.Dtos.ClaimResponse;
import com.claimline.http.Dtos.ReportResponse;
import com.claimline.http.Dtos.SubmitRequest;
import com.claimline.service.ApprovalDeniedException;
import com.claimline.service.ClaimNotFoundException;
import com.claimline.service.ClaimService;
import com.claimline.service.MonthlyReport;
import com.claimline.service.ReportMonth;
import com.claimline.service.ReportService;
import com.claimline.service.SubmitClaimRequest;
import com.claimline.store.Claim;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Maps the HTTP endpoints onto {@link ClaimService} and {@link ReportService}. */
public final class ClaimApi {

  private final ClaimService claims;
  private final ReportService reports;

  public ClaimApi(ClaimService claims, ReportService reports) {
    this.claims = claims;
    this.reports = reports;
  }

  public void register(HttpServer server) {
    server.createContext("/claims", handle(this::routeClaims));
    server.createContext("/reports/monthly", handle(this::routeMonthlyReport));
  }

  private void routeClaims(HttpExchange exchange) throws IOException {
    String method = exchange.getRequestMethod();
    List<String> path = segments(exchange.getRequestURI().getPath());

    // /claims
    if (path.size() == 1) {
      requireMethod(exchange, "POST", method);
      submit(exchange);
      return;
    }
    // /claims/{id}
    if (path.size() == 2) {
      requireMethod(exchange, "GET", method);
      Http.writeJson(exchange, 200, toResponse(claims.get(path.get(1))));
      return;
    }
    // /claims/{id}/approve
    if (path.size() == 3 && path.get(2).equals("approve")) {
      requireMethod(exchange, "POST", method);
      approve(exchange, path.get(1));
      return;
    }
    throw new NotFoundException("no such resource");
  }

  private void submit(HttpExchange exchange) throws IOException {
    SubmitRequest body = Http.readBody(exchange, SubmitRequest.class);
    Claim claim =
        claims.submit(
            new SubmitClaimRequest(
                body.submitterId(), body.amount(), body.category(), body.description()));
    Http.writeJson(exchange, 201, toResponse(claim));
  }

  private void approve(HttpExchange exchange, String claimId) throws IOException {
    ApproveRequest body = Http.readBody(exchange, ApproveRequest.class);
    if (body.approverId() == null || body.approverId().isBlank()) {
      throw new BadRequestException("approverId is required");
    }
    Http.writeJson(exchange, 200, toResponse(claims.approve(claimId, body.approverId())));
  }

  private void routeMonthlyReport(HttpExchange exchange) throws IOException {
    requireMethod(exchange, "GET", exchange.getRequestMethod());
    String month = queryParam(exchange.getRequestURI(), "month");
    if (month == null) {
      throw new BadRequestException("month query parameter is required, formatted YYYY-MM");
    }
    MonthlyReport report = reports.monthly(new ReportMonth(month));
    Http.writeJson(
        exchange,
        200,
        new ReportResponse(report.month(), report.totalsByCategory(), report.total()));
  }

  private static ClaimResponse toResponse(Claim claim) {
    return new ClaimResponse(
        claim.id(),
        claim.submitterId(),
        claim.amount(),
        claim.category(),
        claim.status(),
        claim.approvedBy());
  }

  private HttpHandler handle(ThrowingHandler handler) {
    return exchange -> {
      try {
        handler.handle(exchange);
      } catch (BadRequestException | IllegalArgumentException e) {
        Http.writeError(exchange, 400, e.getMessage());
      } catch (ClaimNotFoundException | NotFoundException e) {
        Http.writeError(exchange, 404, e.getMessage());
      } catch (ApprovalDeniedException e) {
        Http.writeError(exchange, 403, e.getMessage());
      } catch (MethodNotAllowedException e) {
        Http.writeError(exchange, 405, e.getMessage());
      } catch (RuntimeException e) {
        Http.writeError(exchange, 500, "internal error");
      }
    };
  }

  private static void requireMethod(HttpExchange exchange, String expected, String actual) {
    if (!expected.equals(actual)) {
      throw new MethodNotAllowedException(actual + " is not allowed here, expected " + expected);
    }
  }

  /** The non-empty path segments, so that trailing slashes do not change the route. */
  private static List<String> segments(String path) {
    return java.util.Arrays.stream(path.split("/")).filter(s -> !s.isEmpty()).toList();
  }

  private static String queryParam(URI uri, String name) {
    String query = uri.getRawQuery();
    if (query == null) {
      return null;
    }
    for (String pair : query.split("&")) {
      int eq = pair.indexOf('=');
      if (eq > 0 && pair.substring(0, eq).equals(name)) {
        return URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
      }
    }
    return null;
  }

  @FunctionalInterface
  private interface ThrowingHandler {
    void handle(HttpExchange exchange) throws IOException;
  }
}

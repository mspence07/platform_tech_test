package com.claimline.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.claimline.support.TestServices;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClaimApiTest {

  @TempDir Path tempDir;

  private final Gson gson = new Gson();
  private final HttpClient client = HttpClient.newHttpClient();
  private HttpServer server;
  private String baseUrl;

  @BeforeEach
  void startServer() throws IOException {
    Path auditFile = tempDir.resolve("audit-log.txt");
    server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    new ClaimApi(TestServices.claimService(auditFile), TestServices.reportService(auditFile))
        .register(server);
    server.start();
    baseUrl = "http://localhost:" + server.getAddress().getPort();
  }

  @AfterEach
  void stopServer() {
    server.stop(0);
  }

  @Test
  void submitsAndReadsBackAClaim() throws Exception {
    JsonObject submitted = json(post("/claims", claimBody(315, "travel")));
    String id = submitted.get("id").getAsString();

    assertNotNull(id);
    assertEquals("pending", submitted.get("status").getAsString());

    JsonObject fetched = json(get("/claims/" + id));
    assertEquals(315, fetched.get("amount").getAsLong());
  }

  @Test
  void approvingAClaimRecordsTheApprover() throws Exception {
    String id = json(post("/claims", claimBody(315, "travel"))).get("id").getAsString();

    JsonObject approved = json(post("/claims/" + id + "/approve", "{\"approverId\":\"bharat\"}"));

    assertEquals("approved", approved.get("status").getAsString());
    assertEquals("bharat", approved.get("approvedBy").getAsString());
  }

  @Test
  void rejectsAnApproverOverTheirLimitWithForbidden() throws Exception {
    String id = json(post("/claims", claimBody(1_289, "equipment"))).get("id").getAsString();

    assertEquals(
        403, post("/claims/" + id + "/approve", "{\"approverId\":\"alice\"}").statusCode());
  }

  @Test
  void reportsApprovedSpendForAMonth() throws Exception {
    String id = json(post("/claims", claimBody(99, "meals"))).get("id").getAsString();
    post("/claims/" + id + "/approve", "{\"approverId\":\"bharat\"}");

    JsonObject report = json(get("/reports/monthly?month=2026-07"));

    assertEquals(99, report.get("total").getAsLong());
  }

  @Test
  void rejectsAMonthThatIsNotFormattedCorrectly() throws Exception {
    HttpResponse<String> response = get("/reports/monthly?month=2026");

    assertEquals(400, response.statusCode());
    assertTrue(response.body().contains("YYYY-MM"));
  }

  @Test
  void unknownRoutesAreNotFound() throws Exception {
    assertEquals(404, get("/claims/clm-1/nope").statusCode());
  }

  @Test
  void testReadApprovalHistoryForPartiallyApprovedClaim() throws Exception {
    String id = json(post("/claims", claimBody(1_289, "equipment"))).get("id").getAsString();

    post("/claims/" + id + "/approve", "{\"approverId\":\"bharat\"}");

    JsonObject fetched = json(get("/claims/" + id));

    assertEquals("pending", fetched.get("status").getAsString());
    assertEquals(2, fetched.get("approvalsRequired").getAsInt());
    assertEquals(1, fetched.getAsJsonArray("approvals").size());

    JsonObject approval = fetched.getAsJsonArray("approvals").get(0).getAsJsonObject();

    assertEquals("bharat", approval.get("approverId").getAsString());

    assertEquals(TestServices.NOW, approval.get("approvedAt").getAsString());
  }

  private static String claimBody(long amount, String category) {
    return "{\"submitterId\":\"erin\",\"amount\":"
        + amount
        + ",\"category\":\""
        + category
        + "\",\"description\":\"a claim\"}";
  }

  private JsonObject json(HttpResponse<String> response) {
    return gson.fromJson(response.body(), JsonObject.class);
  }

  private HttpResponse<String> post(String path, String body) throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create(baseUrl + path))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> get(String path) throws Exception {
    HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path)).GET().build();
    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }
}

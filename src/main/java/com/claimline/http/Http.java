package com.claimline.http;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Small helpers for reading JSON request bodies and writing JSON responses. */
final class Http {

  private static final Gson GSON = new Gson();

  private Http() {}

  static <T> T readBody(HttpExchange exchange, Class<T> type) throws IOException {
    try (InputStream in = exchange.getRequestBody()) {
      String body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      T parsed = GSON.fromJson(body, type);
      if (parsed == null) {
        throw new BadRequestException("request body is empty");
      }
      return parsed;
    } catch (JsonSyntaxException e) {
      throw new BadRequestException("request body is not valid JSON");
    }
  }

  static void writeJson(HttpExchange exchange, int status, Object body) throws IOException {
    byte[] bytes = GSON.toJson(body).getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "application/json");
    exchange.sendResponseHeaders(status, bytes.length);
    exchange.getResponseBody().write(bytes);
    exchange.close();
  }

  static void writeError(HttpExchange exchange, int status, String message) throws IOException {
    writeJson(exchange, status, Map.of("error", message));
  }
}

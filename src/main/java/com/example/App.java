package com.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Minimal embedded HTTP server for the Student Registration form.
 */
public class App {

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws IOException {
        int port = resolvePort(System.getenv("PORT"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", App::handleHome);
        server.createContext("/register", App::handleRegister);
        server.setExecutor(null);
        server.start();
        System.out.println("Student Registration server running on http://localhost:" + port);
    }

    static int resolvePort(String portEnv) {
        if (portEnv == null || portEnv.isBlank()) {
            return DEFAULT_PORT;
        }
        return Integer.parseInt(portEnv.trim());
    }

    private static void handleHome(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendHtml(exchange, 405, "<h1>Method Not Allowed</h1>");
            return;
        }
        String html = loadResource("/index.html");
        sendHtml(exchange, 200, html);
    }

    private static void handleRegister(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Location", "/");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
            return;
        }
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> fields = parseFormData(body);

        String studentName = escapeHtml(fields.getOrDefault("studentName", ""));
        String rollNumber = escapeHtml(fields.getOrDefault("rollNumber", ""));
        String email = escapeHtml(fields.getOrDefault("email", ""));
        String course = escapeHtml(fields.getOrDefault("course", ""));

        String responseHtml = renderConfirmationHtml(studentName, rollNumber, email, course);
        sendHtml(exchange, 200, responseHtml);
    }

    static String renderConfirmationHtml(String studentName, String rollNumber, String email, String course) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>Registration Confirmation</title>
                    <style>
                        body { font-family: Arial, sans-serif; background: #f4f6f8; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; }
                        .card { background: #fff; padding: 28px 32px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); max-width: 420px; width: 100%; }
                        h2 { color: #198754; margin-top: 0; }
                        p { margin: 8px 0; color: #333; }
                        a { display: inline-block; margin-top: 16px; color: #0d6efd; text-decoration: none; font-weight: bold; }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <h2>Registration Successful</h2>
                        <p><strong>Student Name:</strong> {{studentName}}</p>
                        <p><strong>Roll Number:</strong> {{rollNumber}}</p>
                        <p><strong>Email:</strong> {{email}}</p>
                        <p><strong>Course:</strong> {{course}}</p>
                        <a href="/">Register Another Student</a>
                    </div>
                </body>
                </html>
                """
                .replace("{{studentName}}", studentName)
                .replace("{{rollNumber}}", rollNumber)
                .replace("{{email}}", email)
                .replace("{{course}}", course);
    }

    static String loadResource(String path) throws IOException {
        try (InputStream in = App.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("Resource not found: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    static Map<String, String> parseFormData(String body) {
        Map<String, String> result = new LinkedHashMap<>();
        if (body == null || body.isBlank()) {
            return result;
        }
        for (String pair : body.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                result.put(key, value);
            }
        }
        return result;
    }

    static String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static void sendHtml(HttpExchange exchange, int status, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}

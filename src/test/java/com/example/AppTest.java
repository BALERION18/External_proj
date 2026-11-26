package com.example;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {

    @Test
    void indexHtmlContainsAllRegistrationFields() throws IOException {
        String html = App.loadResource("/index.html");
        assertTrue(html.contains("Student Name"));
        assertTrue(html.contains("Roll Number"));
        assertTrue(html.contains("Email"));
        assertTrue(html.contains("Course"));
        assertTrue(html.contains("Submit"));
    }

    @Test
    void parseFormDataDecodesFields() {
        Map<String, String> parsed = App.parseFormData(
                "studentName=Aarav+Sharma&rollNumber=CS101&email=aarav%40example.com&course=B.Tech+CSE");
        assertEquals("Aarav Sharma", parsed.get("studentName"));
        assertEquals("CS101", parsed.get("rollNumber"));
        assertEquals("aarav@example.com", parsed.get("email"));
        assertEquals("B.Tech CSE", parsed.get("course"));
    }

    @Test
    void escapeHtmlEscapesSpecialCharacters() {
        assertEquals("&lt;script&gt;alert(&quot;x&quot;)&lt;/script&gt;",
                App.escapeHtml("<script>alert(\"x\")</script>"));
    }
}
